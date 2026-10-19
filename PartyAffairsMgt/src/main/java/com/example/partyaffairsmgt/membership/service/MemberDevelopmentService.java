package com.example.partyaffairsmgt.membership.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.member.entity.PartyMember;
import com.example.partyaffairsmgt.member.mapper.PartyMemberMapper;
import com.example.partyaffairsmgt.membership.entity.DevNode;
import com.example.partyaffairsmgt.membership.entity.DevPerson;
import com.example.partyaffairsmgt.membership.entity.DevPlan;
import com.example.partyaffairsmgt.membership.entity.PartyMembership;
import com.example.partyaffairsmgt.membership.mapper.DevNodeMapper;
import com.example.partyaffairsmgt.membership.mapper.DevPersonMapper;
import com.example.partyaffairsmgt.membership.mapper.DevPlanMapper;
import com.example.partyaffairsmgt.membership.mapper.PartyMembershipMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.Period;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 发展党员五阶段全流程服务（需求 5.2，E-DEV-01 ~ E-DEV-13）。
 * 节点串行：代码内节点注册表 MAIN_NODES 定义顺序、审批角色、前置材料；payload_json 承载差异化数据。
 * 注意：E-DEV-04 公示期按需求异常表"按自然日计算"≥5 个自然日（与正文"5个工作日"口径不一致，已在方案注明）。
 */
@Service
public class MemberDevelopmentService {

    public static final String S_APPLICANT = "APPLICANT";
    public static final String S_ACTIVIST = "ACTIVIST";
    public static final String S_TARGET = "TARGET";
    public static final String S_PROBATION = "PROBATION";
    public static final String S_FORMAL = "FORMAL";

    public static final Map<String, String> STAGE_LABEL = Map.of(
            S_APPLICANT, "入党申请人", S_ACTIVIST, "入党积极分子", S_TARGET, "发展对象",
            S_PROBATION, "预备党员", S_FORMAL, "正式党员");

    /** 节点定义：key, 名称, 阶段, 是否需上级党委审批, 是否可重复, 前置材料。 */
    private record Def(String key, String name, String stage, boolean audit,
                       boolean repeatable, List<String> materials) {
    }

    private static final List<Def> MAIN = List.of(
            new Def("APPLY", "递交入党申请/建档", S_APPLICANT, false, false, List.of("入党申请书")),
            new Def("TALK", "党组织派人谈话", S_APPLICANT, false, false, List.of("谈话记录")),
            new Def("RECOMMEND", "党员推荐/群团推优", S_ACTIVIST, false, false, List.of("推荐票或推优表")),
            new Def("BRANCH_DECIDE", "支委会研究确定积极分子", S_ACTIVIST, false, false, List.of("支委会会议记录")),
            new Def("COMMITTEE_FILE", "上级党委备案", S_ACTIVIST, true, false, List.of("上级党委备案批复")),
            new Def("CONTACTS", "指定培养联系人", S_ACTIVIST, false, false, List.of("培养联系人指定记录")),
            new Def("INSPECTION", "半年考察记录", S_ACTIVIST, false, true, List.of("半年考察记录")),
            new Def("OPINIONS", "听取党内外意见", S_TARGET, false, false, List.of("征求意见汇总")),
            new Def("BRANCH_STUDY", "支委会研究并报上级党委同意", S_TARGET, true, false, List.of("支委会研究记录")),
            new Def("PUBLICITY", "确定发展对象公示", S_TARGET, false, false, List.of("公示记录及结果")),
            new Def("INTRODUCERS", "确定2名入党介绍人", S_TARGET, false, false, List.of("入党介绍人指定材料")),
            new Def("POLITICAL_REVIEW", "政治审查", S_TARGET, false, false, List.of("政治审查结论性材料")),
            new Def("TRAINING", "短期集中培训", S_TARGET, false, false, List.of("集中培训结业证明或学时证明")),
            new Def("BRANCH_REVIEW", "支部委员会审查", S_TARGET, false, false, List.of("支委会审查意见")),
            new Def("PRE_AUDIT", "基层党委预审", S_TARGET, true, false, List.of("基层党委预审意见")),
            new Def("MEMBER_MEETING", "支部党员大会讨论表决(接收)", S_PROBATION, false, false, List.of("支部党员大会记录", "表决票")),
            new Def("PARTY_APPROVE", "上级党委审批(接收)", S_PROBATION, true, false, List.of("上级党委审批批复")),
            new Def("OATH", "入党宣誓", S_PROBATION, false, false, List.of("入党宣誓记录")),
            new Def("APPLY_TURN", "提交转正申请", S_FORMAL, false, false, List.of("转正申请书")),
            new Def("OPINIONS2", "征求党内外意见(转正)", S_FORMAL, false, false, List.of("征求意见汇总")),
            new Def("BRANCH_REVIEW2", "支部委员会审查(转正)", S_FORMAL, false, false, List.of("支委会审查记录")),
            new Def("MEETING2", "支部党员大会讨论表决(转正)", S_FORMAL, false, false, List.of("支部党员大会记录", "表决票")),
            new Def("PARTY_APPROVE2", "上级党委审批(转正)", S_FORMAL, true, false, List.of("上级党委审批批复")),
            new Def("DONE", "结果宣布/转正归档", S_FORMAL, false, false, List.of())
    );

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final List<String> FRONTLINE_KEYS = List.of("一线", "生产", "经营", "车间", "班组", "工程");
    private static final List<String> INTEL_EDU = List.of("硕士", "博士", "研究生");

    @Autowired private DevPersonMapper personMapper;
    @Autowired private DevNodeMapper nodeMapper;
    @Autowired private DevPlanMapper planMapper;
    @Autowired private PartyMemberMapper memberMapper;
    @Autowired private PartyMembershipMapper membershipMapper;
    @Autowired private AuditService auditService;

    private Def def(String key) {
        return MAIN.stream().filter(d -> d.key().equals(key)).findFirst()
                .orElseThrow(() -> PartyBizException.block("E-DEV-10", "未知流程节点：" + key));
    }

    private int idx(String key) {
        for (int i = 0; i < MAIN.size(); i++) {
            if (MAIN.get(i).key().equals(key)) return i;
        }
        return -1;
    }

    // ==================== 发展计划 ====================

    public List<DevPlan> planList(Long orgId, Integer year) {
        QueryWrapper<DevPlan> qw = new QueryWrapper<>();
        if (orgId != null) qw.eq("org_id", orgId);
        if (year != null) qw.eq("plan_year", year);
        qw.orderByDesc("plan_year", "org_id");
        return planMapper.selectList(qw);
    }

    @Transactional
    public DevPlan savePlan(DevPlan body) {
        if (body.getOrgId() == null || body.getPlanYear() == null) {
            throw PartyBizException.block("E-DEV-00", "组织与年度必填");
        }
        body.setCreator(CurrentHeader.operator());
        body.setUpdateTime(LocalDateTime.now());
        if (body.getId() == null) {
            body.setCreateTime(LocalDateTime.now());
            planMapper.insert(body);
        } else {
            planMapper.updateById(body);
        }
        return body;
    }

    // ==================== 人员建档 ====================

    public List<DevPerson> personList(String stage, Long orgId, String keyword, String status) {
        QueryWrapper<DevPerson> qw = new QueryWrapper<>();
        if (stage != null && !stage.isBlank()) qw.eq("stage", stage);
        if (orgId != null) qw.eq("org_id", orgId);
        if (status != null && !status.isBlank()) qw.eq("status", status);
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like("name", keyword).or().like("id_card", keyword));
        }
        qw.orderByDesc("create_time");
        return personMapper.selectList(qw);
    }

    @Transactional
    public DevPerson createPerson(DevPerson p) {
        if (p.getName() == null || p.getName().isBlank()) {
            throw PartyBizException.block("E-DEV-01", "申请人姓名必填");
        }
        if (p.getBirthDate() == null) {
            throw PartyBizException.block("E-DEV-01", "出生日期必填，用于年满18周岁校验");
        }
        if (Period.between(p.getBirthDate(), LocalDate.now()).getYears() < 18) {
            throw PartyBizException.block("E-DEV-01", "年满18周岁方可申请入党");
        }
        if (p.getOrgId() == null) {
            throw PartyBizException.block("E-DEV-01", "请选择申请党支部");
        }
        if (p.getIdCard() != null && !p.getIdCard().isBlank()) {
            Long dup = personMapper.selectCount(new QueryWrapper<DevPerson>()
                    .eq("id_card", p.getIdCard()).ne("status", "已取消").ne("status", "已发展"));
            if (dup != null && dup > 0) {
                throw PartyBizException.block("E-DEV-11", "该身份证号已有在途发展记录，不得重复发起同一阶段");
            }
        }
        p.setId(null);
        p.setStage(S_APPLICANT);
        p.setStatus("在途");
        p.setExtendMonths(0);
        p.setCreateTime(LocalDateTime.now());
        p.setUpdateTime(LocalDateTime.now());
        personMapper.insert(p);
        p.setCurrentNode("APPLY");
        personMapper.updateById(p);

        // 建档同时生成 APPLY 节点（未交申请书材料前仍需通过节点办理补齐）
        DevNode apply = new DevNode();
        apply.setPersonId(p.getId());
        apply.setStage(S_APPLICANT);
        apply.setNodeKey("APPLY");
        apply.setNodeName(def("APPLY").name());
        apply.setNodeDate(LocalDate.now());
        apply.setMaterialsJson("[]");
        apply.setNeedAudit(0);
        apply.setAuditStatus("已通过");
        apply.setOperator(CurrentHeader.operator());
        apply.setCreateTime(LocalDateTime.now());
        nodeMapper.insert(apply);
        auditService.log("发展党员建档", "DevPerson", p.getId(), p.getName() + " 入党申请人建档");
        return p;
    }

    // ==================== 节点办理 ====================

    private List<DevNode> passedNodes(Long personId) {
        return nodeMapper.selectList(new QueryWrapper<DevNode>()
                .eq("person_id", personId).eq("audit_status", "已通过"));
    }

    private DevNode nodeRecord(Long personId, String key) {
        return nodeMapper.selectOne(new QueryWrapper<DevNode>()
                .eq("person_id", personId).eq("node_key", key).last("limit 1"));
    }

    /** 办理节点：前置串行校验 + 材料校验 + 节点专属规则。 */
    @Transactional
    public Map<String, Object> completeNode(Long personId, String nodeKey, Map<String, Object> body) {
        DevPerson person = personMapper.selectById(personId);
        if (person == null) throw PartyBizException.block("E-DEV-10", "发展对象不存在");
        if ("已取消".equals(person.getStatus())) {
            throw PartyBizException.block("E-DEV-10", "该发展对象已取消资格，流程终止");
        }
        Def d = def(nodeKey);
        if (!d.repeatable() && nodeRecord(personId, nodeKey) != null) {
            throw PartyBizException.block("E-DEV-11", "同一人不得重复办理节点：" + d.name());
        }

        List<String> warnings = new ArrayList<>();
        // 1) 前置节点串行
        List<String> missingNodes = new ArrayList<>();
        for (int i = 0; i < idx(nodeKey); i++) {
            Def pre = MAIN.get(i);
            if (pre.repeatable()) {
                Long cnt = nodeMapper.selectCount(new QueryWrapper<DevNode>()
                        .eq("person_id", personId).eq("node_key", pre.key())
                        .eq("audit_status", "已通过"));
                if (cnt == null || cnt == 0) missingNodes.add(pre.name());
            } else if (nodeRecord(personId, pre.key()) == null
                    || !"已通过".equals(nodeRecord(personId, pre.key()).getAuditStatus())) {
                missingNodes.add(pre.name());
            }
        }
        if (!missingNodes.isEmpty()) {
            throw PartyBizException.block("E-DEV-10",
                    "前置节点未完成，不得办理[" + d.name() + "]，缺失节点：" + String.join("、", missingNodes));
        }
        // 2) 材料完整性
        @SuppressWarnings("unchecked")
        List<String> materials = body.get("materials") == null ? List.of()
                : (List<String>) body.get("materials");
        // 材料匹配：勾选材料名包含要求项关键字即视为已交
        List<String> lack = new ArrayList<>();
        for (String m : d.materials()) {
            boolean hit = materials.stream().anyMatch(x -> x != null && (x.contains(m) || m.contains(x) && x.length() >= 3));
            if (!hit) lack.add(m);
        }
        if (!lack.isEmpty()) {
            throw PartyBizException.block("E-DEV-10", "节点前置材料缺失，未补齐不得进入下一节点，缺失：" + String.join("、", lack));
        }

        LocalDate nodeDate = body.get("nodeDate") == null ? LocalDate.now()
                : LocalDate.parse(body.get("nodeDate").toString());
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = body.get("payload") == null ? Map.of()
                : (Map<String, Object>) body.get("payload");

        // 3) 节点专属规则
        validateNode(person, d, nodeDate, payload, warnings);

        // 4) 落节点记录
        DevNode n = new DevNode();
        n.setPersonId(personId);
        n.setStage(d.stage());
        n.setNodeKey(nodeKey);
        n.setNodeName(d.name());
        n.setNodeDate(nodeDate);
        try {
            n.setPayloadJson(JSON.writeValueAsString(payload));
            n.setMaterialsJson(JSON.writeValueAsString(materials));
        } catch (Exception ex) {
            throw PartyBizException.block("E-DEV-10", "节点数据序列化失败");
        }
        n.setNeedAudit(d.audit() ? 1 : 0);
        n.setAuditStatus(d.audit() ? "待审批" : "已通过");
        n.setOperator(CurrentHeader.operator());
        n.setRemark(body.get("remark") == null ? null : body.get("remark").toString());
        n.setCreateTime(LocalDateTime.now());
        nodeMapper.insert(n);

        if (!d.audit()) {
            afterPass(person, d, nodeDate, payload, warnings);
        }
        auditService.log("发展节点办理", "DevNode", n.getId(),
                person.getName() + " " + d.name() + (d.audit() ? "（待上级党委审批）" : ""));

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("node", n);
        r.put("warnings", warnings);
        return r;
    }

    /** 节点业务校验（阻断类与预警类）。 */
    private void validateNode(DevPerson person, Def d, LocalDate nodeDate,
                              Map<String, Object> payload, List<String> warnings) {
        switch (d.key()) {
            case "TALK" -> {
                DevNode apply = nodeRecord(person.getId(), "APPLY");
                if (apply != null && apply.getNodeDate() != null
                        && nodeDate.isAfter(apply.getNodeDate().plusDays(30))) {
                    warnings.add("E-DEV-02：收到申请书已超过1个月才完成谈话登记，生成谈话待办提醒（超30天升级告警）");
                }
                if (payload.get("talker") == null || payload.get("talker").toString().isBlank()) {
                    throw PartyBizException.block("E-DEV-10", "谈话节点须填写谈话人");
                }
            }
            case "RECOMMEND" -> {
                if (payload.get("recommendType") == null) {
                    throw PartyBizException.block("E-DEV-10", "须选择推荐方式（党员推荐/群团组织推优）");
                }
            }
            case "CONTACTS" -> {
                int cnt = intVal(payload.get("contactCount"), 0);
                if (cnt < 1 || cnt > 2) {
                    throw PartyBizException.block("E-DEV-10", "培养联系人须为1—2名正式党员");
                }
            }
            case "OPINIONS" -> {
                // E-DEV-03：积极分子培养教育考察满一年方可列为发展对象（以支委会确定日起算）
                DevNode decide = nodeRecord(person.getId(), "BRANCH_DECIDE");
                LocalDate base = decide != null ? decide.getNodeDate() : null;
                if (base == null) base = nodeRecord(person.getId(), "COMMITTEE_FILE") != null
                        ? nodeRecord(person.getId(), "COMMITTEE_FILE").getNodeDate() : null;
                if (base == null || nodeDate.isBefore(base.plusYears(1))) {
                    throw PartyBizException.block("E-DEV-03",
                            "入党积极分子培养教育和考察须满一年以上方可列为发展对象");
                }
            }
            case "PUBLICITY" -> {
                LocalDate s = payload.get("start") == null ? null : LocalDate.parse(payload.get("start").toString());
                LocalDate e = payload.get("end") == null ? null : LocalDate.parse(payload.get("end").toString());
                if (s == null || e == null) {
                    throw PartyBizException.block("E-DEV-04", "须填写公示起止日期");
                }
                // E-DEV-04：按异常表口径按自然日计算，不少于5个自然日
                if (e.isBefore(s.plusDays(5))) {
                    throw PartyBizException.block("E-DEV-04", "发展对象公示期不少于5个自然日（按自然日计算并校验）");
                }
            }
            case "INTRODUCERS" -> {
                int cnt = intVal(payload.get("introducerCount"), 0);
                if (cnt != 2) {
                    throw PartyBizException.block("E-DEV-10", "须确定2名入党介绍人");
                }
            }
            case "POLITICAL_REVIEW" -> {
                boolean qualified = Boolean.TRUE.toString().equals(String.valueOf(payload.get("qualified")))
                        || "合格".equals(String.valueOf(payload.get("conclusion")));
                if (!qualified) {
                    throw PartyBizException.block("E-DEV-05",
                            "未经政治审查或政审不合格不能发展入党");
                }
            }
            case "TRAINING" -> {
                int days = intVal(payload.get("trainingDays"), 0);
                int hours = intVal(payload.get("trainingHours"), 0);
                if (days < 3 && hours < 24) {
                    throw PartyBizException.block("E-DEV-06",
                            "集中培训一般不少于3天或24学时，当前 " + days + " 天/" + hours + " 学时");
                }
            }
            case "MEMBER_MEETING", "MEETING2" -> {
                int should = intVal(payload.get("shouldVote"), 0);
                int actual = intVal(payload.get("actualVote"), 0);
                int approve = intVal(payload.get("approveVotes"), 0);
                if (should <= 0 || actual <= 0) {
                    throw PartyBizException.block("E-DEV-07", "应到/实到有表决权人数必填且大于0");
                }
                if (actual * 2 <= should) {
                    throw PartyBizException.block("E-DEV-07",
                            "实到有表决权党员(" + actual + ")未超过应到(" + should + ")半数，会议无效、不能投票");
                }
                if (approve * 2 <= should) {
                    throw PartyBizException.block("E-DEV-07",
                            "赞成票数(" + approve + ")未超过应到有表决权党员(" + should + ")半数，不能通过");
                }
                if (d.key().equals("MEMBER_MEETING")) {
                    DevNode pre = nodeRecord(person.getId(), "PRE_AUDIT");
                    if (pre != null && pre.getAuditTime() != null
                            && nodeDate.isAfter(pre.getAuditTime().toLocalDate().plusMonths(1))) {
                        warnings.add("基层党委预审合格后超过1个月才召开党员大会，请说明原因");
                    }
                    if (person.getLeaveDate() != null
                            && !person.getLeaveDate().isAfter(nodeDate.plusMonths(3))) {
                        warnings.add("E-DEV-12：发展对象未来三个月内将离开工作学习单位，一般不办理接收预备党员手续");
                    }
                }
            }
            case "PARTY_APPROVE", "PARTY_APPROVE2" -> {
                DevNode meeting = nodeRecord(person.getId(),
                        d.key().equals("PARTY_APPROVE") ? "MEMBER_MEETING" : "MEETING2");
                if (meeting != null && meeting.getNodeDate() != null) {
                    long months = ChronoUnit.MONTHS.between(meeting.getNodeDate(), nodeDate);
                    if (months >= 6) {
                        throw PartyBizException.block("E-DEV-08",
                                "党委审批超过6个月且无延长批准，阻断并上报");
                    }
                    if (months > 3) {
                        warnings.add("E-DEV-08：党委审批已超过3个月（最长不超过6个月），请补充延长批准材料");
                    }
                }
            }
            case "OATH" -> {
                DevNode approve = nodeRecord(person.getId(), "PARTY_APPROVE");
                if (approve != null && approve.getAuditTime() != null
                        && nodeDate.isAfter(approve.getAuditTime().toLocalDate().plusMonths(1))) {
                    warnings.add("批复后超过1个月才组织入党宣誓，请说明原因");
                }
            }
            case "APPLY_TURN" -> {
                LocalDate end = person.getProbationEnd();
                if (end == null) {
                    throw PartyBizException.block("E-DEV-09", "预备期信息缺失，无法办理转正");
                }
                LocalDate limit = person.getExtendMonths() != null && person.getExtendMonths() > 0
                        ? end.plusMonths(person.getExtendMonths()) : end;
                if (nodeDate.isBefore(limit)) {
                    throw PartyBizException.block("E-DEV-09",
                            "预备期未满不得办理转正（期满日：" + limit + "）");
                }
            }
            default -> { /* 其余节点无额外阻断规则 */ }
        }
    }

    /** 节点通过后的主档推进。 */
    private void afterPass(DevPerson person, Def d, LocalDate nodeDate,
                           Map<String, Object> payload, List<String> warnings) {
        person.setUpdateTime(LocalDateTime.now());
        switch (d.key()) {
            case "MEMBER_MEETING" -> {
                person.setStage(S_PROBATION);
                person.setProbationStart(nodeDate);
                person.setProbationEnd(nodeDate.plusYears(1));
                person.setCurrentNode("PARTY_APPROVE");
            }
            case "OATH" -> {
                person.setStage(S_FORMAL);
                person.setCurrentNode("APPLY_TURN");
            }
            case "DONE" -> {
                person.setStage(S_FORMAL);
                person.setStatus("已发展");
                person.setCurrentNode("已完成");
                formalize(person);
            }
            default -> {
                int i = idx(d.key());
                person.setStage(d.stage());
                person.setCurrentNode(i + 1 < MAIN.size() ? MAIN.get(i + 1).key() : "已完成");
            }
        }
        personMapper.updateById(person);
    }

    /** 延长预备期（一次，≥6 月且 ≤12 月，E-DEV-13）/推迟讨论（≤6月）/取消资格。 */
    @Transactional
    public DevPerson sideAction(Long personId, String action, Map<String, Object> body) {
        DevPerson p = personMapper.selectById(personId);
        if (p == null) throw PartyBizException.block("E-DEV-10", "发展对象不存在");
        int months = intVal(body.get("months"), 0);
        String reason = body.get("reason") == null ? "" : body.get("reason").toString();
        switch (action) {
            case "EXTEND" -> {
                if (months < 6 || months > 12) {
                    throw PartyBizException.block("E-DEV-13",
                            "延长预备期不少于半年、最长不超过一年（6—12个月），当前：" + months);
                }
                if (p.getProbationEnd() == null) {
                    throw PartyBizException.block("E-DEV-13", "尚未进入预备期，不能延长");
                }
                p.setExtendMonths(months);
                p.setStatus("已延长");
                auditService.log("延长预备期", "DevPerson", p.getId(), p.getName() + " 延长" + months + "个月：" + reason);
            }
            case "POSTPONE" -> {
                if (months <= 0 || months > 6) {
                    throw PartyBizException.block("E-DEV-10", "推迟讨论转正不超过6个月");
                }
                p.setStatus("已推迟");
                auditService.log("推迟讨论转正", "DevPerson", p.getId(), p.getName() + " 推迟" + months + "个月");
            }
            case "CANCEL" -> {
                if (reason.isBlank()) {
                    throw PartyBizException.block("E-DEV-10", "取消预备党员资格须填写原因");
                }
                p.setStatus("已取消");
                p.setCurrentNode("已取消");
                auditService.log("取消预备党员资格", "DevPerson", p.getId(), p.getName() + "：" + reason);
            }
            default -> throw PartyBizException.block("E-DEV-10", "未知动作：" + action);
        }
        p.setUpdateTime(LocalDateTime.now());
        personMapper.updateById(p);
        return p;
    }

    /** 审批类节点：上级党委审批。 */
    @Transactional
    public DevNode auditNode(Long nodeId, boolean pass, String opinion) {
        DevNode n = nodeMapper.selectById(nodeId);
        if (n == null) throw PartyBizException.block("E-DEV-10", "节点不存在");
        if (n.getNeedAudit() != 1) throw PartyBizException.block("E-DEV-10", "该节点无需上级党委审批");
        if (!"待审批".equals(n.getAuditStatus())) throw PartyBizException.block("E-DEV-10", "该节点已审批");
        n.setAuditStatus(pass ? "已通过" : "已驳回");
        n.setApprover(CurrentHeader.operator());
        n.setAuditOpinion(opinion);
        n.setAuditTime(LocalDateTime.now());
        nodeMapper.updateById(n);
        if (pass) {
            DevPerson person = personMapper.selectById(n.getPersonId());
            Def d = def(n.getNodeKey());
            afterPass(person, d, n.getNodeDate(), parsePayload(n.getPayloadJson()), new ArrayList<>());
        }
        auditService.log(pass ? "发展节点党委批准" : "发展节点党委驳回", "DevNode", nodeId, opinion);
        return n;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parsePayload(String json) {
        try {
            return json == null ? Map.of() : JSON.readValue(json, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }

    /** 转正归档：回写党员主数据与党籍台账（党龄自预备期满转正之日算起）。 */
    private void formalize(DevPerson p) {
        PartyMember member = p.getIdCard() != null ? memberMapper.selectOne(
                new QueryWrapper<PartyMember>().eq("id_card", p.getIdCard()).last("limit 1")) : null;
        boolean isNew = member == null;
        if (isNew) {
            member = new PartyMember();
            member.setName(p.getName());
            member.setGender(p.getGender());
            member.setBirthDate(p.getBirthDate());
            member.setIdCard(p.getIdCard());
            member.setEthnicity(p.getEthnicity());
            member.setEducation(p.getEducation());
            member.setPhone(p.getPhone());
            member.setOrgId(p.getOrgId());
            member.setOrgName(p.getOrgName());
            member.setPosition(p.getPosition());
            member.setJoinDate(p.getProbationStart());
            member.setMemberNo("DY" + p.getId());
            member.setVotingRightSuspended(0);
        }
        member.setMemberStatus("正式党员");
        member.setFullDate(p.getProbationEnd());
        if (isNew) {
            memberMapper.insert(member);
        } else {
            memberMapper.updateById(member);
        }
        PartyMembership ms = membershipMapper.selectOne(new QueryWrapper<PartyMembership>()
                .eq("member_id", member.getId()).last("limit 1"));
        if (ms == null) {
            ms = new PartyMembership();
            ms.setMemberId(member.getId());
            ms.setMemberName(member.getName());
            ms.setMembershipStatus("正式党员");
            ms.setJoinDate(member.getJoinDate());
            ms.setProbationStart(p.getProbationStart());
            ms.setFullMemberDate(p.getProbationEnd());
            ms.setCurrentStatus("正常");
            membershipMapper.insert(ms);
        } else {
            ms.setCurrentStatus("正常");
            ms.setFullMemberDate(p.getProbationEnd());
            membershipMapper.updateById(ms);
        }
        auditService.log("发展转正归档", "PartyMember", member.getId(),
                p.getName() + " 转为正式党员，党龄起算 " + p.getProbationEnd());
    }

    // ==================== 详情 / 看板 ====================

    public Map<String, Object> detail(Long id) {
        DevPerson p = personMapper.selectById(id);
        if (p == null) throw PartyBizException.block("E-DEV-10", "发展对象不存在");
        boolean sensitiveAllowed = PartyRole.hasAny(PartyRole.R02, PartyRole.R03, PartyRole.R04,
                PartyRole.R07, PartyRole.R08);
        List<DevNode> nodes = nodeMapper.selectList(new QueryWrapper<DevNode>()
                .eq("person_id", id).orderByAsc("create_time"));
        List<Map<String, Object>> timeline = new ArrayList<>();
        for (Def d : MAIN) {
            List<DevNode> recs = nodes.stream().filter(n -> n.getNodeKey().equals(d.key())).toList();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("key", d.key());
            row.put("name", d.name());
            row.put("stage", d.stage());
            row.put("needAudit", d.audit());
            row.put("repeatable", d.repeatable());
            row.put("requiredMaterials", d.materials());
            if (recs.isEmpty()) {
                row.put("done", false);
            } else {
                DevNode n = recs.get(0);
                row.put("done", true);
                row.put("nodeId", n.getId());
                row.put("nodeDate", n.getNodeDate());
                row.put("auditStatus", n.getAuditStatus());
                if ("POLITICAL_REVIEW".equals(d.key()) && !sensitiveAllowed) {
                    row.put("payload", Map.of("sensitive", true, "hint", "政审材料为高密级，仅审批链可见"));
                } else {
                    row.put("payload", parsePayload(n.getPayloadJson()));
                }
                if (d.repeatable()) row.put("records", recs.size());
            }
            timeline.add(row);
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("person", p);
        r.put("timeline", timeline);
        r.put("stageLabel", STAGE_LABEL.get(p.getStage()));
        return r;
    }

    /** 看板：五阶段漏斗 + 超时预警 + 结构指标。 */
    public Map<String, Object> board(Long orgId, Integer year) {
        QueryWrapper<DevPerson> qw = new QueryWrapper<>();
        if (orgId != null) qw.eq("org_id", orgId);
        List<DevPerson> all = personMapper.selectList(qw);

        Map<String, Long> funnel = new LinkedHashMap<>();
        for (String s : List.of(S_APPLICANT, S_ACTIVIST, S_TARGET, S_PROBATION)) {
            final String st = s;
            funnel.put(st, all.stream().filter(x -> st.equals(x.getStage()) && "在途".equals(x.getStatus())
                    || st.equals(x.getStage()) && (x.getStatus().contains("延") || x.getStatus().contains("推迟"))).count());
        }
        long formalized = all.stream().filter(x -> "已发展".equals(x.getStatus())).count();
        funnel.put(S_FORMAL, formalized);

        LocalDate now = LocalDate.now();
        int youth = 0;
        int frontline = 0;
        int intellectual = 0;
        for (DevPerson p : all) {
            if (p.getBirthDate() != null && Period.between(p.getBirthDate(), now).getYears() <= 35) youth++;
            String pos = (p.getDepartment() == null ? "" : p.getDepartment()) + (p.getPosition() == null ? "" : p.getPosition());
            if (FRONTLINE_KEYS.stream().anyMatch(pos::contains)) frontline++;
            if (p.getEducation() != null && INTEL_EDU.stream().anyMatch(p.getEducation()::contains)) intellectual++;
        }

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("funnel", funnel);
        r.put("totalInProgress", all.stream().filter(x -> !"已发展".equals(x.getStatus())
                && !"已取消".equals(x.getStatus())).count());
        r.put("structure", Map.of(
                "youth", youth, "frontline", frontline, "intellectual", intellectual,
                "total", all.size(),
                "note", "结构口径：青年≤35岁；一线按部门/岗位含一线/生产/经营/车间等关键词；高知识按学历含硕士/博士/研究生"));
        r.put("timeouts", timeouts(orgId));

        List<Map<String, Object>> planStats = new ArrayList<>();
        List<DevPlan> plans = planList(orgId, year);
        for (DevPlan pl : plans) {
            long done = personMapper.selectCount(new QueryWrapper<DevPerson>()
                    .eq("org_id", pl.getOrgId()).eq("status", "已发展"));
            Map<String, Object> ps = new LinkedHashMap<>();
            ps.put("orgName", pl.getOrgName());
            ps.put("year", pl.getPlanYear());
            ps.put("targetTotal", pl.getTargetTotal());
            ps.put("finished", done);
            ps.put("completionRate", pl.getTargetTotal() == null || pl.getTargetTotal() == 0 ? null
                    : Math.min(100, done * 100 / Math.max(1, pl.getTargetTotal())));
            planStats.add(ps);
        }
        r.put("plans", planStats);
        return r;
    }

    /** 时限超时预警（E-DEV-02 / E-DEV-08 / 宣誓 / 转正审批）。 */
    public List<Map<String, Object>> timeouts(Long orgId) {
        LocalDate now = LocalDate.now();
        List<DevPerson> people = personMapper.selectList(
                orgId == null ? new QueryWrapper<DevPerson>().ne("status", "已取消")
                        : new QueryWrapper<DevPerson>().eq("org_id", orgId).ne("status", "已取消"));
        List<Map<String, Object>> alerts = new ArrayList<>();
        for (DevPerson p : people) {
            addTimeout(alerts, p, "TALK", "APPLY", now, 30,
                    "E-DEV-02", "收到申请书超过1个月未完成谈话登记");
            addTimeout(alerts, p, "PARTY_APPROVE", "MEMBER_MEETING", now, 180,
                    "E-DEV-08", "党委审批接收预备党员超过6个月");
            addTimeout(alerts, p, "OATH", "PARTY_APPROVE", now, 31,
                    "E-DEV-14", "批复后超过1个月未组织入党宣誓");
            addTimeout(alerts, p, "PARTY_APPROVE2", "MEETING2", now, 180,
                    "E-DEV-08", "党委审批转正超过6个月");
        }
        return alerts;
    }

    private void addTimeout(List<Map<String, Object>> alerts, DevPerson p, String pendingKey,
                            String baseKey, LocalDate now, int days, String code, String msg) {
        if (nodeRecord(p.getId(), pendingKey) != null) return;
        DevNode base = nodeRecord(p.getId(), baseKey);
        if (base == null || base.getNodeDate() == null) return;
        LocalDate ref = baseKey.equals("PARTY_APPROVE") && base.getAuditTime() != null
                ? base.getAuditTime().toLocalDate() : base.getNodeDate();
        long elapsed = ChronoUnit.DAYS.between(ref, now);
        if (elapsed > days) {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("personId", p.getId());
            a.put("name", p.getName());
            a.put("orgName", p.getOrgName());
            a.put("code", code);
            a.put("elapsedDays", elapsed);
            a.put("message", msg);
            a.put("level", days >= 180 ? "告警" : "预警");
            alerts.add(a);
        }
    }

    public Map<String, Object> dict() {
        List<Map<String, Object>> defs = new ArrayList<>();
        for (Def d : MAIN) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("key", d.key());
            m.put("name", d.name());
            m.put("stage", d.stage());
            m.put("needAudit", d.audit());
            m.put("repeatable", d.repeatable());
            m.put("materials", d.materials());
            defs.add(m);
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("nodes", defs);
        r.put("stages", STAGE_LABEL);
        return r;
    }

    private int intVal(Object o, int def) {
        if (o == null) return def;
        try {
            return (int) Math.round(Double.parseDouble(o.toString()));
        } catch (Exception e) {
            return def;
        }
    }
}
