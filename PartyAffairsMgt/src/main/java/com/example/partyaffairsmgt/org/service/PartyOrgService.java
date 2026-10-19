package com.example.partyaffairsmgt.org.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.member.entity.PartyMember;
import com.example.partyaffairsmgt.member.mapper.PartyMemberMapper;
import com.example.partyaffairsmgt.membership.entity.DuesPayment;
import com.example.partyaffairsmgt.membership.mapper.DuesPaymentMapper;
import com.example.partyaffairsmgt.org.entity.OrgHistory;
import com.example.partyaffairsmgt.org.entity.OrgMergeRecord;
import com.example.partyaffairsmgt.org.entity.OrgLifeMeeting;
import com.example.partyaffairsmgt.org.entity.PartyOrg;
import com.example.partyaffairsmgt.org.entity.TermElection;
import com.example.partyaffairsmgt.org.mapper.OrgHistoryMapper;
import com.example.partyaffairsmgt.org.mapper.OrgMergeRecordMapper;
import com.example.partyaffairsmgt.org.mapper.OrgLifeMeetingMapper;
import com.example.partyaffairsmgt.org.mapper.PartyOrgMapper;
import com.example.partyaffairsmgt.org.mapper.TermElectionMapper;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 4.1 组织架构管理业务服务：组织树、编码、合规校验、审批、合并撤销、导入导出、届满提醒。 */
@Service
public class PartyOrgService {

    public static final Set<String> ORG_TYPES = new HashSet<>(Arrays.asList(
            "党组", "党委", "党总支", "党支部", "党小组", "临时党支部"));
    public static final Set<String> ORG_STATUS = new HashSet<>(Arrays.asList(
            "筹备", "正常", "换届中", "合并", "撤销"));

    private static final int TERM_WARN_DAYS = 180;
    private static final int TERM_OVERDUE_DAYS = 90;

    @Autowired private PartyOrgMapper orgMapper;
    @Autowired private PartyMemberMapper memberMapper;
    @Autowired private OrgHistoryMapper historyMapper;
    @Autowired private OrgMergeRecordMapper mergeMapper;
    @Autowired private TermElectionMapper electionMapper;
    @Autowired private OrgLifeMeetingMapper meetingMapper;
    @Autowired private DuesPaymentMapper duesMapper;
    @Autowired private AuditService auditService;

    // ==================== 组织树 ====================

    /** 懒加载：返回某父节点的直接下级；keyword 非空时返回全量匹配（含路径）供搜索定位。 */
    public List<Map<String, Object>> tree(Long parentId, String keyword, String orgType) {
        if (keyword != null && !keyword.isBlank()) {
            QueryWrapper<PartyOrg> qw = new QueryWrapper<>();
            qw.and(w -> w.like("name", keyword).or().like("org_code", keyword))
              .eq("audit_status", "APPROVED").ne("status", "撤销");
            if (orgType != null && !orgType.isBlank()) {
                qw.eq("org_type", orgType);
            }
            List<PartyOrg> hits = orgMapper.selectList(qw);
            List<Map<String, Object>> result = new ArrayList<>();
            for (PartyOrg o : hits) {
                Map<String, Object> node = toTreeNode(o, false);
                node.put("matched", true);
                node.put("path", breadcrumbNames(o.getId()));
                result.add(node);
            }
            return result;
        }
        QueryWrapper<PartyOrg> qw = new QueryWrapper<>();
        if (parentId == null) {
            qw.isNull("parent_id");
        } else {
            qw.eq("parent_id", parentId);
        }
        qw.eq("audit_status", "APPROVED").ne("status", "撤销").orderByAsc("sort_order", "id");
        if (orgType != null && !orgType.isBlank()) {
            qw.eq("org_type", orgType);
        }
        List<PartyOrg> orgs = orgMapper.selectList(qw);
        List<Map<String, Object>> nodes = new ArrayList<>();
        for (PartyOrg o : orgs) {
            nodes.add(toTreeNode(o, true));
        }
        return nodes;
    }

    private Map<String, Object> toTreeNode(PartyOrg o, boolean withChildrenFlag) {
        Map<String, Object> n = new LinkedHashMap<>();
        n.put("id", o.getId());
        n.put("orgCode", o.getOrgCode());
        n.put("name", o.getName());
        n.put("shortName", o.getShortName());
        n.put("orgType", o.getOrgType());
        n.put("parentId", o.getParentId());
        n.put("status", o.getStatus());
        n.put("secretary", o.getSecretary());
        n.put("memberCount", countMembers(o.getId()).get("total"));
        n.put("termStatus", termStatus(o));
        n.put("verifyFlag", o.getVerifyFlag());
        n.put("isFloating", o.getIsFloating());
        List<String> alerts = new ArrayList<>();
        if ("EXPIRED".equals(termStatus(o))) {
            alerts.add("届满超期未换届(E-ORG-06)");
        } else if ("NEAR".equals(termStatus(o))) {
            alerts.add("临近换届(E-ORG-06)");
        }
        n.put("alerts", alerts);
        if (withChildrenFlag) {
            n.put("hasChildren", orgMapper.selectCount(
                    new QueryWrapper<PartyOrg>().eq("parent_id", o.getId())
                            .eq("audit_status", "APPROVED").ne("status", "撤销")) > 0);
        }
        return n;
    }

    /** NORMAL / NEAR(届满前180天内，黄) / EXPIRED(超届满90天，红) / BETWEEN(已届满90天内) / NONE */
    public String termStatus(PartyOrg o) {
        if (o.getTermEnd() == null) {
            return "NONE";
        }
        LocalDate today = LocalDate.now();
        LocalDate end = o.getTermEnd();
        if (today.isAfter(end.plusDays(TERM_OVERDUE_DAYS))) {
            return "EXPIRED";
        }
        if (today.isAfter(end)) {
            return "BETWEEN";
        }
        if (!today.isBefore(end.minusDays(TERM_WARN_DAYS))) {
            return "NEAR";
        }
        return "NORMAL";
    }

    // ==================== 详情聚合 ====================

    public Map<String, Object> detail(Long id) {
        PartyOrg o = mustGet(id);
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("org", o);
        d.put("members", membersOfOrg(id, false));
        Map<String, Integer> counts = countMembers(id);
        d.put("memberCount", counts.get("total"));
        d.put("formalCount", counts.get("formal"));
        d.put("breadcrumb", breadcrumb(id));
        d.put("histories", historyMapper.selectList(
                new QueryWrapper<OrgHistory>().eq("org_id", id).orderByDesc("change_date", "id")));
        TermElection latest = electionMapper.selectOne(new QueryWrapper<TermElection>()
                .eq("org_id", id).orderByDesc("election_date", "id").last("limit 1"));
        d.put("latestElection", latest);
        int year = LocalDate.now().getYear();
        Map<String, Object> lifeStats = new LinkedHashMap<>();
        for (String t : Arrays.asList("民主生活会", "组织生活会")) {
            long held = meetingMapper.selectCount(new QueryWrapper<OrgLifeMeeting>()
                    .eq("org_id", id).eq("meeting_type", t)
                    .eq("status", "已召开").apply("year(meeting_date)={0}", year));
            lifeStats.put(t + "_已开", held);
            lifeStats.put(t + "_要求", 1);
        }
        d.put("lifeStats", lifeStats);
        return d;
    }

    public List<PartyMember> membersOfOrg(Long orgId, boolean includeSubTree) {
        QueryWrapper<PartyMember> qw = new QueryWrapper<>();
        if (includeSubTree) {
            List<Long> ids = subtreeIds(orgId);
            if (ids.isEmpty()) {
                return new ArrayList<>();
            }
            qw.in("org_id", ids);
        } else {
            qw.eq("org_id", orgId);
        }
        qw.orderByAsc("id");
        return memberMapper.selectList(qw);
    }

    public Map<String, Integer> countMembers(Long orgId) {
        List<PartyMember> all = membersOfOrg(orgId, false);
        int total = all.size();
        int formal = (int) all.stream().filter(m -> "正式党员".equals(m.getMemberStatus())).count();
        int voting = (int) all.stream()
                .filter(m -> "正式党员".equals(m.getMemberStatus()))
                .filter(m -> m.getVotingRightSuspended() == null || m.getVotingRightSuspended() == 0)
                .count();
        Map<String, Integer> r = new LinkedHashMap<>();
        r.put("total", total);
        r.put("formal", formal);
        r.put("voting", voting);
        return r;
    }

    public List<Long> subtreeIds(Long rootId) {
        List<Long> ids = new ArrayList<>();
        if (rootId == null) {
            return ids;
        }
        ids.add(rootId);
        List<Long> frontier = new ArrayList<>(ids);
        while (!frontier.isEmpty()) {
            List<PartyOrg> children = orgMapper.selectList(new QueryWrapper<PartyOrg>()
                    .in("parent_id", frontier).ne("status", "撤销"));
            frontier = children.stream().map(PartyOrg::getId).collect(Collectors.toList());
            ids.addAll(frontier);
        }
        return ids;
    }

    public List<Map<String, Object>> breadcrumb(Long id) {
        List<Map<String, Object>> chain = new ArrayList<>();
        PartyOrg cur = orgMapper.selectById(id);
        Set<Long> guard = new HashSet<>();
        while (cur != null && guard.add(cur.getId())) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", cur.getId());
            m.put("name", cur.getName());
            chain.add(0, m);
            cur = cur.getParentId() == null ? null : orgMapper.selectById(cur.getParentId());
        }
        return chain;
    }

    public List<String> breadcrumbNames(Long id) {
        return breadcrumb(id).stream().map(m -> (String) m.get("name")).collect(Collectors.toList());
    }

    // ==================== 新建 / 编辑 ====================

    /**
     * 新建组织：编码自动生成；默认进入待审核(PENDING/筹备)，R03 批复后挂载。
     * 若携带 approvalNo（已有批复文件），R02 可直接批复入库。
     */
    @Transactional
    public Map<String, Object> create(PartyOrg org, boolean directApprove) {
        validateRequired(org);
        validateTypeCompatible(org.getOrgType());
        PartyOrg parent = org.getParentId() == null ? null : mustGet(org.getParentId());
        checkParentCompatibility(org.getOrgType(), parent);
        // E-ORG-01 编码重复（正常编码由系统生成，此处兜底防外部传入）
        if (org.getOrgCode() != null && !org.getOrgCode().isBlank()
                && orgMapper.selectCount(new QueryWrapper<PartyOrg>().eq("org_code", org.getOrgCode())) > 0) {
            throw PartyBizException.block("E-ORG-01", "组织编码已存在：" + org.getOrgCode());
        }
        org.setOrgCode(generateCode(parent));
        List<String> warnings = complianceWarnings(org);
        org.setVerifyFlag(warnings.isEmpty() ? 0 : 1);
        org.setStatus(org.getStatus() == null ? "筹备" : org.getStatus());
        LocalDateTime now = LocalDateTime.now();
        org.setCreateTime(now);
        org.setUpdateTime(now);
        if (directApprove) {
            org.setAuditStatus("APPROVED");
            if (org.getApprovalNo() != null && !org.getApprovalNo().isBlank()) {
                org.setStatus("正常");
            }
        } else {
            org.setAuditStatus("PENDING");
            org.setStatus("筹备");
        }
        if (org.getIsFloating() == null) {
            org.setIsFloating(0);
        }
        orgMapper.insert(org);
        writeHistory(org.getId(), "设立", null, org.getName() + "(" + org.getOrgCode() + ")",
                org.getApprovalNo(), "新建组织提交");
        if (directApprove) {
            auditService.log("ORG_CREATE_APPROVED", "party_org", org.getId(),
                    "直接批复设立 " + org.getName());
        } else {
            auditService.log("ORG_CREATE_SUBMIT", "party_org", org.getId(), "提交设立审核 " + org.getName());
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("org", orgMapper.selectById(org.getId()));
        r.put("warnings", warnings);
        return r;
    }

    /** R03 批复：登记批复文号 → 状态正常、挂载组织树。 */
    @Transactional
    public PartyOrg approve(Long id, String approvalNo) {
        PartyOrg o = mustGet(id);
        if (approvalNo == null || approvalNo.isBlank()) {
            throw PartyBizException.block("E-ORG-01", "批复编号不能为空");
        }
        o.setApprovalNo(approvalNo);
        o.setAuditStatus("APPROVED");
        o.setStatus("正常");
        o.setUpdateTime(LocalDateTime.now());
        orgMapper.updateById(o);
        writeHistory(id, "批复", null, approvalNo, approvalNo, "上级党委组织部门批复登记");
        auditService.log("ORG_APPROVE", "party_org", id, "批复设立 " + approvalNo);
        return o;
    }

    @Transactional
    public Map<String, Object> update(PartyOrg org) {
        PartyOrg db = mustGet(org.getId());
        // E-ORG-01：编码不可修改
        if (org.getOrgCode() != null && !org.getOrgCode().equals(db.getOrgCode())) {
            throw PartyBizException.block("E-ORG-01", "组织编码一经生成不可修改（撤销后编码保留不重用）");
        }
        org.setOrgCode(db.getOrgCode());
        validateRequired(org);
        validateTypeCompatible(org.getOrgType());
        boolean parentChanged = org.getParentId() != null
                && !org.getParentId().equals(db.getParentId());
        if (parentChanged) {
            PartyOrg target = org.getParentId() == null ? null : mustGet(org.getParentId());
            checkParentCompatibility(org.getOrgType(), target);
            if (target != null && subtreeIds(db.getId()).contains(target.getId())) {
                throw PartyBizException.block("E-ORG-04", "不能将组织挂载到其下级组织下");
            }
        }
        List<String> warnings = complianceWarnings(org);
        org.setVerifyFlag(warnings.isEmpty() ? 0 : 1);
        org.setAuditStatus(db.getAuditStatus());
        org.setUpdateTime(LocalDateTime.now());
        orgMapper.updateById(org);
        if (parentChanged) {
            writeHistory(org.getId(), "隶属调整",
                    db.getParentId() == null ? "-" : db.getParentId().toString(),
                    org.getParentId() == null ? "-" : org.getParentId().toString(),
                    null, "调整隶属关系");
            auditService.log("ORG_MOVE", "party_org", org.getId(), "隶属关系调整");
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("org", orgMapper.selectById(org.getId()));
        r.put("warnings", warnings);
        return r;
    }

    /** 拖拽调整层级（前端二次确认后调用）。 */
    @Transactional
    public PartyOrg move(Long id, Long newParentId) {
        PartyOrg o = mustGet(id);
        PartyOrg target = newParentId == null ? null : mustGet(newParentId);
        checkParentCompatibility(o.getOrgType(), target);
        if (target != null && subtreeIds(id).contains(target.getId())) {
            throw PartyBizException.block("E-ORG-04", "不能将组织挂载到其下级组织下");
        }
        Long old = o.getParentId();
        o.setParentId(newParentId);
        o.setUpdateTime(LocalDateTime.now());
        orgMapper.updateById(o);
        writeHistory(id, "隶属调整", old == null ? "根" : old.toString(),
                newParentId == null ? "根" : newParentId.toString(), null, "拖拽调整层级");
        auditService.log("ORG_DRAG_MOVE", "party_org", id,
                "由 " + old + " 调整为 " + newParentId);
        return o;
    }

    /** R07 党支书记维护本支部党小组（partyGroups JSON）。 */
    @Transactional
    public PartyOrg updatePartyGroups(Long orgId, String partyGroupsJson) {
        PartyOrg o = mustGet(orgId);
        if (!"党支部".equals(o.getOrgType()) && !"临时党支部".equals(o.getOrgType())) {
            throw PartyBizException.block("E-ORG-04", "仅党支部可划分党小组");
        }
        o.setPartyGroups(partyGroupsJson);
        o.setUpdateTime(LocalDateTime.now());
        orgMapper.updateById(o);
        auditService.log("ORG_PARTY_GROUP", "party_org", orgId, "维护党小组划分");
        return o;
    }

    // ==================== 校验算法 ====================

    private void validateRequired(PartyOrg o) {
        if (o.getName() == null || o.getName().isBlank()) {
            throw PartyBizException.block("E-ORG-01", "组织名称必填");
        }
        if (o.getName().length() > 50) {
            throw PartyBizException.block("E-ORG-01", "组织名称不超过50字");
        }
        if (!ORG_TYPES.contains(o.getOrgType())) {
            throw PartyBizException.block("E-ORG-01", "组织类型非法：" + o.getOrgType());
        }
        if (o.getEstablishedDate() == null) {
            throw PartyBizException.block("E-ORG-01", "成立日期必填（以批复或成立文件日期为准）");
        }
        if (o.getStatus() != null && !ORG_STATUS.contains(o.getStatus())) {
            throw PartyBizException.block("E-ORG-01", "组织状态非法：" + o.getStatus());
        }
    }

    private void validateTypeCompatible(String t) {
        if (!ORG_TYPES.contains(t)) {
            throw PartyBizException.block("E-ORG-01", "组织类型非法");
        }
    }

    /** E-ORG-04：上级组织类型兼容矩阵。 */
    public void checkParentCompatibility(String childType, PartyOrg parent) {
        if (childType == null) {
            throw PartyBizException.block("E-ORG-04", "组织类型不能为空");
        }
        if (childType.equals("党小组")) {
            if (parent == null || (!"党支部".equals(parent.getOrgType())
                    && !"临时党支部".equals(parent.getOrgType()))) {
                throw PartyBizException.block("E-ORG-04", "党小组只能隶属于党支部");
            }
            return;
        }
        if (childType.equals("党总支")) {
            if (parent == null || (!"党委".equals(parent.getOrgType()) && !"党组".equals(parent.getOrgType()))) {
                throw PartyBizException.block("E-ORG-04", "党总支应隶属于党委（或党组）");
            }
            return;
        }
        if (childType.equals("党支部") || childType.equals("临时党支部")) {
            if (parent == null) {
                throw PartyBizException.block("E-ORG-04", "党支部应隶属于党委或党总支，根节点为集团党委");
            }
            if (!Arrays.asList("党委", "党总支").contains(parent.getOrgType())) {
                throw PartyBizException.block("E-ORG-04", "党支部的目标上级应为党委或党总支");
            }
            return;
        }
        if (childType.equals("党委")) {
            if (parent != null && !"党委".equals(parent.getOrgType()) && !"党组".equals(parent.getOrgType())) {
                throw PartyBizException.block("E-ORG-04", "党委的目标上级应为上级党委或党组");
            }
        }
    }

    /** E-ORG-02/E-ORG-05：按在册正式党员数做合规预警（不阻断，标记待核实）。 */
    public List<String> complianceWarnings(PartyOrg o) {
        List<String> warnings = new ArrayList<>();
        boolean temporary = "临时党支部".equals(o.getOrgType()) || "党小组".equals(o.getOrgType())
                || "党组".equals(o.getOrgType());
        if (temporary) {
            return warnings;
        }
        int formal = countMembers(o.getId() == null ? -1L : o.getId()).get("formal");
        // 新建尚未落库时按 0 计算（无法统计），此时以前端随单提交 memberCountHint 不做要求
        if (o.getId() == null) {
            return warnings;
        }
        if (formal == 0) {
            warnings.add("E-ORG-05：该组织无在册党员，请核实（临时党组织除外）");
            return warnings;
        }
        switch (o.getOrgType()) {
            case "党委":
                if (formal < 100) {
                    warnings.add("E-ORG-02：正式党员" + formal + "人，不足100人设立党委须经上级党组织批准");
                }
                break;
            case "党总支":
                if (formal < 50) {
                    warnings.add("E-ORG-02：正式党员" + formal + "人，不足50人设立党总支须经上级党组织批准");
                } else if (formal >= 100) {
                    warnings.add("E-ORG-02：正式党员" + formal + "人已达100人，一般应设立党委");
                }
                break;
            case "党支部":
                if (formal < 3) {
                    warnings.add("E-ORG-02：正式党员仅" + formal + "人，成立党支部须有正式党员3人以上");
                } else if (formal < 7) {
                    warnings.add("E-ORG-02：正式党员不足7人，设书记1名、必要时设副书记，不设支部委员会");
                }
                break;
            default:
                break;
        }
        return warnings;
    }

    /** 组织编码：上级编码.同级序号（两位），序号按历史创建顺序，撤销不重用。 */
    public synchronized String generateCode(PartyOrg parent) {
        String prefix = parent == null ? "" : parent.getOrgCode() + ".";
        String likePrefix = prefix.isEmpty() ? "" : prefix;
        List<PartyOrg> siblings;
        if (parent == null) {
            siblings = orgMapper.selectList(new QueryWrapper<PartyOrg>().isNull("parent_id"));
        } else {
            siblings = orgMapper.selectList(new QueryWrapper<PartyOrg>()
                    .likeRight("org_code", likePrefix));
            // likeRight 会把更深层节点一并查出，用段数过滤为直接下级
            int segCount = parent.getOrgCode().split("\\.").length + 1;
            siblings = siblings.stream()
                    .filter(s -> s.getOrgCode() != null && s.getOrgCode().split("\\.").length == segCount)
                    .collect(Collectors.toList());
        }
        int max = 0;
        for (PartyOrg s : siblings) {
            String last = s.getOrgCode().contains(".")
                    ? s.getOrgCode().substring(s.getOrgCode().lastIndexOf('.') + 1)
                    : s.getOrgCode();
            try {
                max = Math.max(max, Integer.parseInt(last));
            } catch (NumberFormatException ignored) {
                // 兼容历史非数字编码
            }
        }
        int next = max + 1;
        String tail = next < 10 ? "0" + next : String.valueOf(next);
        return prefix.isEmpty() ? tail : prefix + tail;
    }

    // ==================== 合并 / 撤销 影响分析与执行 ====================

    public Map<String, Object> impactAnalysis(Long sourceOrgId, Long targetOrgId, String bizType) {
        PartyOrg source = mustGet(sourceOrgId);
        List<Long> subIds = subtreeIds(sourceOrgId);
        int members = memberMapper.selectCount(new QueryWrapper<PartyMember>().in("org_id", subIds)).intValue();
        long childCount = orgMapper.selectCount(new QueryWrapper<PartyOrg>()
                .eq("parent_id", sourceOrgId).ne("status", "撤销"));
        long pendingElections = electionMapper.selectCount(new QueryWrapper<TermElection>()
                .in("org_id", subIds).and(w -> w.ne("stage", "FILED").or().isNull("stage")));
        long pendingMeetings = meetingMapper.selectCount(new QueryWrapper<OrgLifeMeeting>()
                .in("org_id", subIds).eq("status", "计划中"));
        long unpaidDues = duesMapper.selectCount(new QueryWrapper<DuesPayment>()
                .in("org_id", subIds)
                .and(w -> w.isNull("pay_date").or().like("status", "欠").or().like("status", "未")));

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("bizType", bizType);
        r.put("sourceOrg", source);
        r.put("targetOrgId", targetOrgId);
        if (targetOrgId != null) {
            r.put("targetOrg", orgMapper.selectById(targetOrgId));
        }
        r.put("affectedMembers", members);
        r.put("childOrgCount", childCount);
        r.put("pendingFlows", pendingElections + pendingMeetings);
        r.put("unpaidDues", unpaidDues);
        List<String> blockers = new ArrayList<>();
        if (childCount > 0) {
            blockers.add("存在下级组织 " + childCount + " 个");
        }
        if (pendingElections + pendingMeetings > 0) {
            blockers.add("存在在办事项（换届/会议）" + (pendingElections + pendingMeetings) + " 项");
        }
        if (unpaidDues > 0) {
            blockers.add("存在未结党费 " + unpaidDues + " 人次");
        }
        r.put("blocked", !blockers.isEmpty());
        r.put("blockerDetails", blockers);
        return r;
    }

    @Transactional
    public OrgMergeRecord executeMerge(Long sourceOrgId, Long targetOrgId, String reason) {
        if (sourceOrgId.equals(targetOrgId)) {
            throw PartyBizException.block("E-ORG-03", "被合并组织与目标组织不能相同");
        }
        PartyOrg target = mustGet(targetOrgId);
        Map<String, Object> impact = impactAnalysis(sourceOrgId, targetOrgId, "MERGE");
        if (Boolean.TRUE.equals(impact.get("blocked"))) {
            throw PartyBizException.block("E-ORG-03",
                    "合并被阻断：" + ((List<?>) impact.get("blockerDetails")).stream()
                            .map(Object::toString).collect(Collectors.joining("；")));
        }
        PartyOrg source = mustGet(sourceOrgId);
        List<Long> subIds = subtreeIds(sourceOrgId);
        // 党员归属迁移并通知（留痕模拟）
        List<PartyMember> members = memberMapper.selectList(new QueryWrapper<PartyMember>().in("org_id", subIds));
        final int migratedCount = members.size();
        for (PartyMember m : members) {
            m.setOrgId(targetOrgId);
            m.setOrgName(target.getName());
            m.setUpdateTime(LocalDateTime.now());
            memberMapper.updateById(m);
        }
        source.setStatus("合并");
        source.setUpdateTime(LocalDateTime.now());
        orgMapper.updateById(source);
        writeHistory(sourceOrgId, "合并", source.getOrgCode(), target.getOrgCode(), null,
                "合并至" + target.getName() + (reason == null ? "" : "；原因：" + reason));
        OrgMergeRecord rec = saveMergeRecord("MERGE", source, target, impact, reason);
        auditService.log("ORG_MERGE", "party_org", sourceOrgId,
                source.getName() + " 合并至 " + target.getName() + "，迁移党员" + migratedCount + "人");
        return rec;
    }

    @Transactional
    public OrgMergeRecord executeRevoke(Long orgId, String reason) {
        Map<String, Object> impact = impactAnalysis(orgId, null, "REVOKE");
        if (Boolean.TRUE.equals(impact.get("blocked"))) {
            throw PartyBizException.block("E-ORG-03",
                    "撤销被阻断：" + ((List<?>) impact.get("blockerDetails")).stream()
                            .map(Object::toString).collect(Collectors.joining("；")));
        }
        PartyOrg o = mustGet(orgId);
        o.setStatus("撤销");
        o.setUpdateTime(LocalDateTime.now());
        orgMapper.updateById(o); // 编码保留不重用
        writeHistory(orgId, "撤销", o.getOrgCode(), null, null,
                "组织撤销" + (reason == null ? "" : "；原因：" + reason));
        OrgMergeRecord rec = saveMergeRecord("REVOKE", o, null, impact, reason);
        auditService.log("ORG_REVOKE", "party_org", orgId, "撤销组织 " + o.getName());
        return rec;
    }

    private OrgMergeRecord saveMergeRecord(String type, PartyOrg source, PartyOrg target,
                                           Map<String, Object> impact, String reason) {
        OrgMergeRecord rec = new OrgMergeRecord();
        rec.setBizType(type);
        rec.setSourceOrgId(source.getId());
        rec.setSourceOrgName(source.getName());
        rec.setSourceCode(source.getOrgCode());
        if (target != null) {
            rec.setTargetOrgId(target.getId());
            rec.setTargetOrgName(target.getName());
        }
        rec.setAffectedMembers((Integer) impact.get("affectedMembers"));
        rec.setChildOrgCount(((Number) impact.get("childOrgCount")).intValue());
        rec.setPendingFlows((Integer) impact.get("pendingFlows"));
        rec.setUnpaidDues((Integer) impact.get("unpaidDues"));
        rec.setReason(reason);
        rec.setStatus("EXECUTED");
        rec.setOperator(CurrentHeader.operator());
        rec.setCreateTime(LocalDateTime.now());
        rec.setUpdateTime(LocalDateTime.now());
        mergeMapper.insert(rec);
        return rec;
    }

    /** 删除（区别于撤销：仅允许删除无成员无下级的筹备/待审核组织）。 */
    @Transactional
    public void delete(Long id) {
        PartyOrg o = mustGet(id);
        long children = orgMapper.selectCount(new QueryWrapper<PartyOrg>().eq("parent_id", id));
        int members = memberMapper.selectCount(new QueryWrapper<PartyMember>().eq("org_id", id)).intValue();
        if (children > 0 || members > 0) {
            throw PartyBizException.block("E-ORG-03",
                    "删除被阻断：下级组织" + children + "个、在册党员" + members + "人；请改用撤销/合并流程");
        }
        orgMapper.deleteById(id);
        auditService.log("ORG_DELETE", "party_org", id, "删除组织 " + o.getName());
    }

    // ==================== 届满提醒（E-ORG-06） ====================

    public Map<String, Object> termAlerts() {
        List<PartyOrg> all = orgMapper.selectList(new QueryWrapper<PartyOrg>()
                .eq("audit_status", "APPROVED").ne("status", "撤销"));
        List<Map<String, Object>> near = new ArrayList<>();
        List<Map<String, Object>> expired = new ArrayList<>();
        for (PartyOrg o : all) {
            String s = termStatus(o);
            if ("NEAR".equals(s)) {
                near.add(alertRow(o));
            } else if ("EXPIRED".equals(s)) {
                expired.add(alertRow(o));
            }
        }
        Comparator<Map<String, Object>> cmp = Comparator.comparing(
                r -> (String) ((Map<String, Object>) r).get("termEnd"));
        near.sort(cmp);
        expired.sort(cmp);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("near", near);
        r.put("expired", expired);
        return r;
    }

    private Map<String, Object> alertRow(PartyOrg o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("name", o.getName());
        m.put("orgType", o.getOrgType());
        m.put("termEnd", o.getTermEnd() == null ? null : o.getTermEnd().toString());
        m.put("daysToEnd", o.getTermEnd() == null ? null
                : java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), o.getTermEnd()));
        return m;
    }

    // ==================== 列表页 / 导出 / 导入 ====================

    public Map<String, Object> siblingPage(Long parentId, Integer pageNum, Integer pageSize, String keyword) {
        QueryWrapper<PartyOrg> qw = new QueryWrapper<>();
        if (parentId == null) {
            qw.isNull("parent_id");
        } else {
            qw.eq("parent_id", parentId);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like("name", keyword).or().like("org_code", keyword));
        }
        qw.orderByAsc("sort_order", "id");
        List<PartyOrg> all = orgMapper.selectList(qw);
        int total = all.size();
        List<PartyOrg> page = all.stream()
                .skip((long) (pageNum - 1) * pageSize).limit(pageSize).collect(Collectors.toList());
        List<Map<String, Object>> rows = page.stream().map(o -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", o.getId());
            row.put("orgCode", o.getOrgCode());
            row.put("name", o.getName());
            row.put("orgType", o.getOrgType());
            row.put("secretary", o.getSecretary());
            row.put("establishedDate", o.getEstablishedDate());
            row.put("termEnd", o.getTermEnd());
            row.put("status", o.getStatus());
            row.put("memberCount", countMembers(o.getId()).get("total"));
            row.put("termStatus", termStatus(o));
            return row;
        }).collect(Collectors.toList());
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("records", rows);
        r.put("total", total);
        r.put("pageNum", pageNum);
        r.put("pageSize", pageSize);
        return r;
    }

    /** 批量导出当前层级（含编码/名称/类型/书记/党员数/届满/状态），需授权并记审计。 */
    public String exportCsv(Long parentId) {
        Map<String, Object> page = siblingPage(parentId, 1, 100000, null);
        StringBuilder sb = new StringBuilder("﻿组织编码,组织名称,组织类型,书记,成立日期,届满日期,状态,党员数,届满状态\n");
        for (Map<String, Object> row : (List<Map<String, Object>>) page.get("records")) {
            sb.append(csv(row.get("orgCode"))).append(',')
              .append(csv(row.get("name"))).append(',')
              .append(csv(row.get("orgType"))).append(',')
              .append(csv(row.get("secretary"))).append(',')
              .append(csv(row.get("establishedDate"))).append(',')
              .append(csv(row.get("termEnd"))).append(',')
              .append(csv(row.get("status"))).append(',')
              .append(row.get("memberCount")).append(',')
              .append(csv(row.get("termStatus"))).append('\n');
        }
        auditService.log("ORG_EXPORT", "party_org", parentId,
                "导出组织列表 " + ((List<?>) page.get("records")).size() + " 行");
        return sb.toString();
    }

    private String csv(Object v) {
        if (v == null) {
            return "";
        }
        String s = v.toString();
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }

    /** Excel 导入模板字节。 */
    public byte[] importTemplate() {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("组织导入");
            String[] headers = {"组织名称*", "组织类型*", "上级编码", "成立日期*(YYYY-MM-DD)",
                    "书记", "状态", "批复文号", "是否流动党组织(是/否)", "备注"};
            Row head = sheet.createRow(0);
            CellStyle style = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            style.setFont(font);
            for (int i = 0; i < headers.length; i++) {
                Cell c = head.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(style);
                sheet.setColumnWidth(i, 5000);
            }
            Row demo = sheet.createRow(1);
            String[] vals = {"示例党支部", "党支部", "01.01", "2026-03-01", "张三", "正常", "某党组〔2026〕1号", "否", "模板示例行，导入前删除"};
            for (int i = 0; i < vals.length; i++) {
                demo.createCell(i).setCellValue(vals[i]);
            }
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("模板生成失败：" + e.getMessage(), e);
        }
    }

    /** 解析并逐行校验导入文件（E-ORG-07：错误行定位），不落库。 */
    public Map<String, Object> importPreview(byte[] fileBytes) {
        List<Map<String, Object>> validRows = new ArrayList<>();
        List<Map<String, Object>> errorRows = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(new java.io.ByteArrayInputStream(fileBytes))) {
            Sheet sheet = wb.getSheetAt(0);
            int last = sheet.getLastRowNum();
            for (int r = 1; r <= last; r++) {
                Row row = sheet.getRow(r);
                if (row == null || isBlankRow(row)) {
                    continue;
                }
                int rowNo = r + 1;
                String name = str(row, 0);
                String type = str(row, 1);
                String parentCode = str(row, 2);
                String dateStr = str(row, 3);
                String secretary = str(row, 4);
                String status = str(row, 5);
                String approvalNo = str(row, 6);
                String floating = str(row, 7);
                String remark = str(row, 8);
                List<String> errs = new ArrayList<>();
                if (name.isBlank()) {
                    errs.add("组织名称为空");
                } else if (name.length() > 50) {
                    errs.add("组织名称超过50字");
                }
                if (!ORG_TYPES.contains(type)) {
                    errs.add("组织类型非法：" + type);
                }
                LocalDate established = null;
                if (dateStr.isBlank()) {
                    errs.add("成立日期为空");
                } else {
                    try {
                        established = LocalDate.parse(dateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                    } catch (Exception e) {
                        errs.add("成立日期格式错误，应为YYYY-MM-DD：" + dateStr);
                    }
                }
                Long parentId = null;
                if (!parentCode.isBlank()) {
                    PartyOrg parent = orgMapper.selectOne(new QueryWrapper<PartyOrg>()
                            .eq("org_code", parentCode.trim()).last("limit 1"));
                    if (parent == null) {
                        errs.add("上级编码不存在：" + parentCode);
                    } else {
                        parentId = parent.getId();
                        try {
                            checkParentCompatibility(type, parent);
                        } catch (PartyBizException ex) {
                            errs.add(ex.getMessage());
                        }
                    }
                } else if (!"党委".equals(type) && !"党组".equals(type)) {
                    errs.add("非根节点组织必须填写上级编码");
                }
                if (!status.isBlank() && !ORG_STATUS.contains(status)) {
                    errs.add("状态非法：" + status);
                }
                Map<String, Object> data = new HashMap<>();
                data.put("rowNo", rowNo);
                data.put("name", name);
                data.put("orgType", type);
                data.put("parentId", parentId);
                data.put("parentCode", parentCode);
                data.put("establishedDate", established);
                data.put("secretary", secretary);
                data.put("status", status.isBlank() ? "正常" : status);
                data.put("approvalNo", approvalNo);
                data.put("isFloating", "是".equals(floating.trim()) ? 1 : 0);
                data.put("remark", remark);
                if (errs.isEmpty()) {
                    validRows.add(data);
                } else {
                    data.put("errors", errs);
                    errorRows.add(data);
                }
            }
        } catch (PartyBizException e) {
            throw e;
        } catch (Exception e) {
            throw PartyBizException.block("E-ORG-07", "Excel解析失败：" + e.getMessage());
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("validRows", validRows);
        r.put("errorRows", errorRows);
        r.put("importable", errorRows.isEmpty());
        return r;
    }

    /** 选择性提交正确行（可只导入勾选行）。 */
    @Transactional
    public int importCommit(List<Map<String, Object>> rows) {
        int n = 0;
        for (Map<String, Object> row : rows) {
            PartyOrg o = new PartyOrg();
            o.setName((String) row.get("name"));
            o.setOrgType((String) row.get("orgType"));
            Object pid = row.get("parentId");
            o.setParentId(pid == null ? null : Long.valueOf(pid.toString()));
            o.setEstablishedDate(LocalDate.parse(row.get("establishedDate").toString()));
            o.setSecretary((String) row.get("secretary"));
            o.setStatus((String) row.get("status"));
            o.setApprovalNo((String) row.get("approvalNo"));
            Object f = row.get("isFloating");
            o.setIsFloating(f == null ? 0 : Integer.valueOf(f.toString()));
            o.setRemark((String) row.get("remark"));
            PartyOrg parent = o.getParentId() == null ? null : mustGet(o.getParentId());
            o.setOrgCode(generateCode(parent));
            o.setAuditStatus("APPROVED");
            o.setVerifyFlag(0);
            LocalDateTime now = LocalDateTime.now();
            o.setCreateTime(now);
            o.setUpdateTime(now);
            orgMapper.insert(o);
            writeHistory(o.getId(), "设立", null, o.getName() + "(" + o.getOrgCode() + ")",
                    o.getApprovalNo(), "批量导入");
            n++;
        }
        auditService.log("ORG_IMPORT", "party_org", null, "批量导入组织 " + n + " 个");
        return n;
    }

    // ==================== 待审核列表 / 辅助 ====================

    public List<PartyOrg> pendingList() {
        return orgMapper.selectList(new QueryWrapper<PartyOrg>()
                .eq("audit_status", "PENDING").orderByAsc("create_time"));
    }

    private boolean isBlankRow(Row row) {
        for (int c = 0; c < 9; c++) {
            Cell cell = row.getCell(c);
            if (cell != null && !str(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String str(Row row, int i) {
        Cell cell = row.getCell(i);
        return str(cell);
    }

    private String str(Cell cell) {
        if (cell == null) {
            return "";
        }
        try {
            return switch (cell.getCellType()) {
                case STRING -> cell.getStringCellValue().trim();
                case NUMERIC -> {
                    double d = cell.getNumericCellValue();
                    if (d == Math.floor(d)) {
                        yield String.valueOf((long) d);
                    }
                    yield String.valueOf(d);
                }
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                default -> "";
            };
        } catch (Exception e) {
            return "";
        }
    }

    private PartyOrg mustGet(Long id) {
        PartyOrg o = orgMapper.selectById(id);
        if (o == null) {
            throw PartyBizException.block("E-ORG-01", "组织不存在：" + id);
        }
        return o;
    }

    public void writeHistory(Long orgId, String type, String from, String to,
                             String approvalNo, String remark) {
        OrgHistory h = new OrgHistory();
        h.setOrgId(orgId);
        h.setChangeType(type);
        h.setFromValue(from);
        h.setToValue(to);
        h.setChangeDate(LocalDate.now());
        h.setApprovalNo(approvalNo);
        h.setOperator(CurrentHeader.operator());
        h.setRemark(remark);
        h.setCreateTime(LocalDateTime.now());
        h.setUpdateTime(LocalDateTime.now());
        historyMapper.insert(h);
    }

    /** 任期年限：党委5年；党总支/党支部委员会3年。 */
    public int termYears(String orgType) {
        return "党委".equals(orgType) ? 5 : 3;
    }
}
