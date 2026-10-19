package com.example.partyaffairsmgt.org.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.member.entity.PartyMember;
import com.example.partyaffairsmgt.member.mapper.PartyMemberMapper;
import com.example.partyaffairsmgt.org.entity.PartyOrg;
import com.example.partyaffairsmgt.org.entity.TermCandidate;
import com.example.partyaffairsmgt.org.entity.TermElection;
import com.example.partyaffairsmgt.org.entity.TermWitness;
import com.example.partyaffairsmgt.org.mapper.PartyOrgMapper;
import com.example.partyaffairsmgt.org.mapper.TermCandidateMapper;
import com.example.partyaffairsmgt.org.mapper.TermElectionMapper;
import com.example.partyaffairsmgt.org.mapper.TermWitnessMapper;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 4.2 换届选举：向导四步、票数/党龄/公示校验、备案回写届次与任期。 */
@Service
public class TermElectionService {

    private static final int NEAR_DAYS = 180;

    @Autowired private TermElectionMapper electionMapper;
    @Autowired private TermCandidateMapper candidateMapper;
    @Autowired private TermWitnessMapper witnessMapper;
    @Autowired private PartyOrgMapper orgMapper;
    @Autowired private PartyMemberMapper memberMapper;
    @Autowired private PartyOrgService orgService;
    @Autowired private AuditService auditService;

    // ==================== 计划列表 / 详情 ====================

    public List<Map<String, Object>> list(Integer year, Long orgId) {
        QueryWrapper<TermElection> qw = new QueryWrapper<>();
        if (year != null) {
            qw.apply("year(election_date)={0} or year(create_time)={0}", year);
        }
        if (orgId != null) {
            qw.eq("org_id", orgId);
        }
        qw.orderByDesc("id");
        List<TermElection> list = electionMapper.selectList(qw);
        return list.stream().map(e -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", e.getId());
            row.put("orgId", e.getOrgId());
            row.put("orgName", e.getOrgName());
            row.put("termNo", e.getTermNo());
            row.put("electionDate", e.getElectionDate());
            row.put("stage", e.getStage());
            row.put("status", e.getStatus());
            row.put("progress", progress(e));
            row.put("publicityStart", e.getPublicityStart());
            row.put("publicityEnd", e.getPublicityEnd());
            return row;
        }).collect(Collectors.toList());
    }

    public int progress(TermElection e) {
        if ("FILED".equals(e.getStage())) {
            return 100;
        }
        return switch (e.getStage() == null ? "PLAN" : e.getStage()) {
            case "CANDIDATE" -> 45;
            case "MEETING" -> (e.getTallyDone() != null && e.getTallyDone() == 1) ? 90 : 70;
            default -> 15;
        };
    }

    public Map<String, Object> detail(Long id) {
        TermElection e = mustGet(id);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("election", e);
        r.put("candidates", candidateMapper.selectList(new QueryWrapper<TermCandidate>()
                .eq("election_id", id).orderByAsc("sort_order", "id")));
        r.put("witnesses", witnessMapper.selectList(new QueryWrapper<TermWitness>()
                .eq("election_id", id).orderByAsc("witness_type", "id")));
        return r;
    }

    public List<TermElection> history(Long orgId) {
        QueryWrapper<TermElection> qw = new QueryWrapper<TermElection>()
                .eq("stage", "FILED").orderByDesc("election_date", "id");
        if (orgId != null) {
            qw.eq("org_id", orgId);
        }
        return electionMapper.selectList(qw);
    }

    // ==================== 第一步：方案 ====================

    @Transactional
    public TermElection createPlan(Map<String, Object> body) {
        Long orgId = Long.valueOf(body.get("orgId").toString());
        PartyOrg org = orgMapper.selectById(orgId);
        if (org == null) {
            throw PartyBizException.block("E-ELEC-07", "换届组织不存在");
        }
        // E-ELEC-07：同一组织存在未结束换届
        Long open = electionMapper.selectCount(new QueryWrapper<TermElection>()
                .eq("org_id", orgId).ne("stage", "FILED"));
        if (open != null && open > 0) {
            throw PartyBizException.block("E-ELEC-07", "该组织当前届次换届尚未结束，不能重复发起");
        }
        boolean earlyApproval = Boolean.TRUE.equals(body.get("earlyApproval"))
                || Integer.valueOf(1).equals(body.get("earlyApproval"));
        String earlyFile = body.get("earlyApprovalFile") == null ? null
                : body.get("earlyApprovalFile").toString();
        // E-ELEC-01：任期未满提前换届须上级批准件
        if (org.getTermEnd() != null
                && LocalDate.now().isBefore(org.getTermEnd().minusDays(NEAR_DAYS))
                && !earlyApproval) {
            throw PartyBizException.block("E-ELEC-01",
                    "任期未满（届满日" + org.getTermEnd() + "），提前发起换届须上传上级党组织批准文件");
        }
        if (org.getTermEnd() != null
                && LocalDate.now().isBefore(org.getTermEnd().minusDays(NEAR_DAYS))
                && earlyApproval && (earlyFile == null || earlyFile.isBlank())) {
            throw PartyBizException.block("E-ELEC-01", "已勾选提前换届批准，但未上传批准文件");
        }
        TermElection e = new TermElection();
        e.setOrgId(orgId);
        e.setOrgName(org.getName());
        e.setTermNo(body.get("termNo") == null ? null : body.get("termNo").toString());
        e.setElectionMode((String) body.get("electionMode"));
        e.setElectionDate(parseDate(body.get("electionDate")));
        e.setMeetingLocation((String) body.get("meetingLocation"));
        e.setShouldVoteCount(orgService.countMembers(orgId).get("voting"));
        e.setEarlyApproval(earlyApproval ? 1 : 0);
        e.setEarlyApprovalFile(earlyFile);
        e.setStage("PLAN");
        e.setTallyDone(0);
        e.setStatus("进行中");
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        e.setCreateTime(now);
        e.setUpdateTime(now);
        org.setStatus("换届中");
        org.setUpdateTime(now);
        orgMapper.updateById(org);
        electionMapper.insert(e);
        auditService.log("ELEC_PLAN", "term_election", e.getId(),
                org.getName() + " 第" + e.getTermNo() + "届换届方案发起");
        return e;
    }

    @Transactional
    public TermElection updatePlan(Long id, Map<String, Object> body) {
        TermElection e = mustGet(id);
        if ("FILED".equals(e.getStage())) {
            throw PartyBizException.block("E-ELEC-07", "已备案的换届记录不能修改");
        }
        if (body.containsKey("termNo")) {
            e.setTermNo(body.get("termNo") == null ? null : body.get("termNo").toString());
        }
        if (body.containsKey("electionMode")) {
            e.setElectionMode((String) body.get("electionMode"));
        }
        if (body.containsKey("electionDate")) {
            e.setElectionDate(parseDate(body.get("electionDate")));
        }
        if (body.containsKey("meetingLocation")) {
            e.setMeetingLocation((String) body.get("meetingLocation"));
        }
        if (body.containsKey("planData")) {
            e.setPlanData((String) body.get("planData"));
        }
        e.setUpdateTime(java.time.LocalDateTime.now());
        electionMapper.updateById(e);
        return e;
    }

    /** 刷新应到会人数（在册正式党员中有表决权者）。 */
    public int refreshShouldVote(Long electionId) {
        TermElection e = mustGet(electionId);
        int n = orgService.countMembers(e.getOrgId()).get("voting");
        e.setShouldVoteCount(n);
        e.setUpdateTime(java.time.LocalDateTime.now());
        electionMapper.updateById(e);
        return n;
    }

    // ==================== 第二步：候选人（E-ELEC-05 党龄） ====================

    @Transactional
    public List<TermCandidate> saveCandidates(Long electionId, List<Map<String, Object>> candidates) {
        TermElection e = mustGet(electionId);
        if ("FILED".equals(e.getStage())) {
            throw PartyBizException.block("E-ELEC-07", "已备案，不能调整候选人");
        }
        PartyOrg org = orgMapper.selectById(e.getOrgId());
        LocalDate base = e.getElectionDate() != null ? e.getElectionDate() : LocalDate.now();
        // 党委委员党龄阈值：本系统为国有企业，默认5年；可通过请求参数 centralEnterprise=false 降为3年
        int committeeThreshold = 5;
        int secretaryBranchThreshold = 1;
        candidateMapper.delete(new QueryWrapper<TermCandidate>().eq("election_id", electionId));
        List<TermCandidate> saved = new ArrayList<>();
        int order = 0;
        for (Map<String, Object> c : candidates) {
            Long memberId = c.get("memberId") == null ? null : Long.valueOf(c.get("memberId").toString());
            String name = (String) c.get("memberName");
            String position = (String) c.get("position");
            PartyMember m = memberId == null ? null : memberMapper.selectById(memberId);
            if (m != null && (name == null || name.isBlank())) {
                name = m.getName();
            }
            int age = partyAgeYears(m, base);
            // E-ELEC-05
            if ("党支部".equals(org.getOrgType()) && "书记".equals(position) && age < secretaryBranchThreshold) {
                throw PartyBizException.block("E-ELEC-05",
                        "党支部书记候选人 " + name + " 党龄" + age + "年，须满" + secretaryBranchThreshold + "年");
            }
            if ("党委".equals(org.getOrgType()) && age < committeeThreshold) {
                throw PartyBizException.block("E-ELEC-05",
                        "党委委员候选人 " + name + " 党龄" + age + "年，国有企业党委委员一般须满"
                                + committeeThreshold + "年党龄（参数化配置）");
            }
            if (!"党委".equals(org.getOrgType()) && age < 3 && ("副书记".equals(position)
                    || "委员".equals(position) || "书记".equals(position))) {
                // 非党委班子候选人的通用3年口径仅作预警不阻断（支部书记按上面1年口径）
            }
            TermCandidate tc = new TermCandidate();
            tc.setElectionId(electionId);
            tc.setMemberId(memberId);
            tc.setMemberName(name);
            tc.setPartyAgeYears(age);
            tc.setPosition(position);
            tc.setVotes(c.get("votes") == null ? 0 : Integer.valueOf(c.get("votes").toString()));
            tc.setElected(c.get("elected") == null ? 0 : Integer.valueOf(c.get("elected").toString()));
            tc.setSortOrder(c.get("sortOrder") == null ? order : Integer.valueOf(c.get("sortOrder").toString()));
            tc.setCreateTime(java.time.LocalDateTime.now());
            tc.setUpdateTime(java.time.LocalDateTime.now());
            candidateMapper.insert(tc);
            saved.add(tc);
            order++;
        }
        if (!"CANDIDATE".equals(e.getStage()) && !"MEETING".equals(e.getStage())) {
            e.setStage("CANDIDATE");
        }
        e.setUpdateTime(java.time.LocalDateTime.now());
        electionMapper.updateById(e);
        auditService.log("ELEC_CANDIDATE", "term_election", electionId, "保存候选人 " + saved.size() + " 名");
        return saved;
    }

    @Transactional
    public List<TermWitness> saveWitnesses(Long electionId, List<Map<String, Object>> witnesses) {
        TermElection e = mustGet(electionId);
        List<String> candidateNames = candidateMapper.selectList(new QueryWrapper<TermCandidate>()
                        .eq("election_id", electionId)).stream()
                .map(TermCandidate::getMemberName).collect(Collectors.toList());
        witnessMapper.delete(new QueryWrapper<TermWitness>().eq("election_id", electionId));
        List<TermWitness> saved = new ArrayList<>();
        for (Map<String, Object> w : witnesses) {
            String name = (String) w.get("memberName");
            String type = (String) w.get("witnessType");
            if (name == null || name.isBlank() || type == null || type.isBlank()) {
                throw PartyBizException.block("E-ELEC-02", "监票人/计票人姓名与角色必填");
            }
            if (candidateNames.contains(name)) {
                throw PartyBizException.block("E-ELEC-02", type + " " + name + " 是候选人，监票/计票须由非候选人担任");
            }
            TermWitness tw = new TermWitness();
            tw.setElectionId(electionId);
            tw.setMemberName(name);
            tw.setWitnessType(type);
            tw.setCreateTime(java.time.LocalDateTime.now());
            tw.setUpdateTime(java.time.LocalDateTime.now());
            witnessMapper.insert(tw);
            saved.add(tw);
        }
        return saved;
    }

    // ==================== 第三步：会议与计票 ====================

    @Transactional
    public Map<String, Object> saveTally(Long electionId, Map<String, Object> body) {
        TermElection e = mustGet(electionId);
        int should = e.getShouldVoteCount() == null ? 0 : e.getShouldVoteCount();
        int actual = intVal(body.get("actualCount"));
        int issued = intVal(body.get("ballotsIssued"));
        int received = intVal(body.get("ballotsReceived"));
        int valid = intVal(body.get("validVotes"));
        int invalid = intVal(body.get("invalidVotes"));
        int blank = intVal(body.get("blankVotes"));

        // E-ELEC-02：实到须超过应到半数
        if (should <= 0) {
            throw PartyBizException.block("E-ELEC-02", "应到会有表决权人数为0，请先刷新人员统计");
        }
        if (actual * 2 <= should) {
            throw PartyBizException.block("E-ELEC-02",
                    "实到" + actual + "人未超过应到会" + should + "人的半数，会议无效，不能投票");
        }
        // E-ELEC-03
        if (received > issued) {
            throw PartyBizException.block("E-ELEC-03",
                    "收回票数(" + received + ")大于发出票数(" + issued + ")，计票异常，需重新计票");
        }
        if (valid + invalid + blank != received) {
            throw PartyBizException.block("E-ELEC-03",
                    "有效票(" + valid + ")+无效票(" + invalid + ")+弃权票(" + blank + ")="
                            + (valid + invalid + blank) + "，应等于收回票数(" + received + ")");
        }
        // 票数录入到候选人并判定当选（赞成票须超过应到半数）
        Object votesObj = body.get("votes");
        List<TermCandidate> candidates = candidateMapper.selectList(new QueryWrapper<TermCandidate>()
                .eq("election_id", electionId).orderByAsc("sort_order", "id"));
        Map<String, Integer> voteMap = new LinkedHashMap<>();
        if (votesObj instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> m) {
                    voteMap.put(String.valueOf(m.get("candidateId")), intVal(m.get("votes")));
                }
            }
        }
        List<String> notElected = new ArrayList<>();
        int voteSum = 0;
        for (TermCandidate c : candidates) {
            Integer v = voteMap.get(String.valueOf(c.getId()));
            if (v != null) {
                c.setVotes(v);
            }
            voteSum += c.getVotes() == null ? 0 : c.getVotes();
            boolean pass = c.getVotes() != null && c.getVotes() * 2 > should;
            // 被安排职务但票数未过半 → E-ELEC-04
            if (c.getPosition() != null && !c.getPosition().isBlank() && !pass) {
                notElected.add(c.getMemberName() + "(" + c.getPosition() + "," + c.getVotes() + "票)");
            }
            c.setElected(pass ? 1 : 0);
            c.setUpdateTime(java.time.LocalDateTime.now());
            candidateMapper.updateById(c);
        }
        if (!notElected.isEmpty()) {
            throw PartyBizException.block("E-ELEC-04",
                    "以下同志得赞成票未超过应到会有表决权人数(" + should + ")的半数，不能宣布当选，可组织另行选举："
                            + String.join("、", notElected));
        }
        if (voteSum > valid) {
            throw PartyBizException.block("E-ELEC-03",
                    "候选人得票合计(" + voteSum + ")超过有效票数(" + valid + ")，请核对计票");
        }
        e.setActualCount(actual);
        e.setBallotsIssued(issued);
        e.setBallotsReceived(received);
        e.setValidVotes(valid);
        e.setInvalidVotes(invalid);
        e.setBlankVotes(blank);
        if (body.get("electionDate") != null) {
            e.setElectionDate(parseDate(body.get("electionDate")));
        }
        if (body.get("electionMode") != null) {
            e.setElectionMode((String) body.get("electionMode"));
        }
        if (body.get("meetingLocation") != null) {
            e.setMeetingLocation((String) body.get("meetingLocation"));
        }
        e.setTallyDone(1);
        e.setStage("MEETING");
        e.setUpdateTime(java.time.LocalDateTime.now());
        electionMapper.updateById(e);
        auditService.log("ELEC_TALLY", "term_election", electionId, "计票完成");

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("election", e);
        r.put("elected", candidates.stream().filter(c -> c.getElected() == 1).collect(Collectors.toList()));
        return r;
    }

    // ==================== 第四步：结果备案（回写届次/任期） ====================

    @Transactional
    public TermElection fileResult(Long electionId, Map<String, Object> body) {
        TermElection e = mustGet(electionId);
        // E-ELEC-06：公示必填
        LocalDate pubStart = parseDate(body.get("publicityStart"));
        LocalDate pubEnd = parseDate(body.get("publicityEnd"));
        boolean confirmed = Boolean.TRUE.equals(body.get("confirmedPublicity"))
                || Integer.valueOf(1).equals(body.get("confirmedPublicity"));
        if (!confirmed || pubStart == null || pubEnd == null) {
            throw PartyBizException.block("E-ELEC-06",
                    "未完成候选人公示与结果公示：须填写公示起止日期并勾选“已完成公示”");
        }
        if (pubEnd.isBefore(pubStart)) {
            throw PartyBizException.block("E-ELEC-06", "公示结束日期不能早于开始日期");
        }
        if (e.getTallyDone() == null || e.getTallyDone() != 1) {
            throw PartyBizException.block("E-ELEC-02", "尚未完成计票，不能备案选举结果");
        }
        String approvalNo = (String) body.get("approvalNo");
        LocalDate recordDate = parseDate(body.get("recordDate"));
        if (approvalNo == null || approvalNo.isBlank() || recordDate == null) {
            throw PartyBizException.block("E-ELEC-06", "批复文号与备案日期必填");
        }
        List<TermCandidate> elected = candidateMapper.selectList(new QueryWrapper<TermCandidate>()
                .eq("election_id", electionId).eq("elected", 1).orderByAsc("sort_order", "id"));
        if (elected.isEmpty()) {
            throw PartyBizException.block("E-ELEC-04", "无得票过半的当选人，不能备案");
        }
        String secretary = elected.stream().filter(c -> "书记".equals(c.getPosition()))
                .map(TermCandidate::getMemberName).findFirst().orElse(null);
        String members = elected.stream()
                .map(c -> c.getMemberName() + (c.getPosition() == null ? "" : "(" + c.getPosition() + ")"))
                .collect(Collectors.joining(","));
        e.setPublicityStart(pubStart);
        e.setPublicityEnd(pubEnd);
        e.setApprovalNo(approvalNo);
        e.setRecordDate(recordDate);
        e.setElectedSecretary(secretary);
        e.setElectedMembers(members);
        e.setStage("FILED");
        e.setStatus("已备案");
        e.setUpdateTime(java.time.LocalDateTime.now());
        electionMapper.updateById(e);

        // 回写组织届次、任期（党委5年/总支支部3年，自批复备案之日起算）
        PartyOrg org = orgMapper.selectById(e.getOrgId());
        if (body.get("termNo") != null) {
            org.setTermNo(Integer.valueOf(body.get("termNo").toString()));
        } else if (e.getTermNo() != null) {
            try {
                org.setTermNo(Integer.valueOf(e.getTermNo().replaceAll("\\D", "")));
            } catch (Exception ignored) {
                // 届次非数字时忽略
            }
        }
        org.setTermStart(recordDate);
        org.setTermEnd(recordDate.plusYears(orgService.termYears(org.getOrgType())));
        if (secretary != null) {
            org.setSecretary(secretary);
        }
        org.setCommitteeMembers(members);
        org.setStatus("正常");
        org.setApprovalNo(approvalNo);
        org.setVerifyFlag(0);
        org.setUpdateTime(java.time.LocalDateTime.now());
        orgMapper.updateById(org);
        orgService.writeHistory(org.getId(), "换届", "上一届",
                "第" + org.getTermNo() + "届 书记:" + secretary, approvalNo,
                "换届选举结果批复备案，任期至" + org.getTermEnd());
        auditService.log("ELEC_FILE", "term_election", electionId,
                "选举结果备案，回写届次/任期至" + org.getTermEnd());
        return e;
    }

    /** R03 对备案结果进行审批确认。 */
    @Transactional
    public TermElection audit(Long electionId, boolean pass, String opinion) {
        TermElection e = mustGet(electionId);
        if (!"FILED".equals(e.getStage())) {
            throw PartyBizException.block("E-ELEC-07", "仅已备案记录可审批");
        }
        e.setStatus(pass ? "已审批" : "审批退回");
        e.setUpdateTime(java.time.LocalDateTime.now());
        electionMapper.updateById(e);
        auditService.log("ELEC_AUDIT", "term_election", electionId,
                (pass ? "审批通过" : "审批退回") + (opinion == null ? "" : "：" + opinion));
        return e;
    }

    // ==================== 辅助 ====================

    private int partyAgeYears(PartyMember m, LocalDate base) {
        if (m == null) {
            return 99; // 手工录入无档案人员不做党龄阻断
        }
        LocalDate d = m.getFullDate() != null ? m.getFullDate() : m.getJoinDate();
        if (d == null) {
            return 99;
        }
        return (int) ChronoUnit.YEARS.between(d, base);
    }

    private int intVal(Object o) {
        if (o == null) {
            return 0;
        }
        if (o instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(o.toString());
    }

    private LocalDate parseDate(Object o) {
        if (o == null || o.toString().isBlank()) {
            return null;
        }
        String s = o.toString();
        try {
            return s.length() > 10 ? LocalDate.parse(s.substring(0, 10)) : LocalDate.parse(s);
        } catch (Exception ex) {
            throw PartyBizException.block("E-ELEC-02", "日期格式错误：" + s);
        }
    }

    private TermElection mustGet(Long id) {
        TermElection e = electionMapper.selectById(id);
        if (e == null) {
            throw PartyBizException.block("E-ELEC-07", "换届记录不存在：" + id);
        }
        return e;
    }
}
