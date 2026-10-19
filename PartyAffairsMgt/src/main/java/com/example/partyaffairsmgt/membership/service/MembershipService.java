package com.example.partyaffairsmgt.membership.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.member.entity.PartyMember;
import com.example.partyaffairsmgt.member.mapper.PartyMemberMapper;
import com.example.partyaffairsmgt.membership.entity.MembershipChange;
import com.example.partyaffairsmgt.membership.entity.MembershipProof;
import com.example.partyaffairsmgt.membership.entity.PartyMembership;
import com.example.partyaffairsmgt.membership.mapper.MembershipChangeMapper;
import com.example.partyaffairsmgt.membership.mapper.MembershipProofMapper;
import com.example.partyaffairsmgt.membership.mapper.PartyMembershipMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 党籍管理服务（需求 5.1）。
 * 状态机：正常→停止/自行脱党/除名/开除/死亡注销/转出；停止党籍→恢复党籍；转出→转入；终态冻结。
 * 所有变动须上级党委审批通过后才回写台账（未审批先行生效阻断，E-MS-03）。
 */
@Service
public class MembershipService {

    public static final String NORMAL = "正常";
    public static final String STOP = "停止党籍";
    public static final String RESTORE = "恢复党籍";
    public static final String DESERTER = "自行脱党";
    public static final String REMOVE = "除名";
    public static final String EXPEL = "开除党籍";
    public static final String DEAD = "死亡注销";
    public static final String TRANSFER_OUT = "转出";
    public static final String TRANSFER_IN = "转入";

    /** 终态：不得再发起任何变动。 */
    private static final Set<String> TERMINAL = Set.of(REMOVE, EXPEL, DEAD, DESERTER);

    /** 合法流转表：当前状态 -> 可发起的变动类型。 */
    private static final Map<String, List<String>> TRANSITIONS = Map.of(
            NORMAL, List.of(STOP, DESERTER, REMOVE, EXPEL, DEAD, TRANSFER_OUT, TRANSFER_IN),
            STOP, List.of(RESTORE),
            TRANSFER_OUT, List.of(TRANSFER_IN)
    );

    @Autowired
    private MembershipChangeMapper changeMapper;
    @Autowired
    private MembershipProofMapper proofMapper;
    @Autowired
    private PartyMembershipMapper membershipMapper;
    @Autowired
    private PartyMemberMapper memberMapper;
    @Autowired
    private AuditService auditService;

    /** 当前党籍状态（无 party_membership 记录视为正常）。 */
    public String currentStatus(Long memberId) {
        PartyMembership m = membershipMapper.selectOne(
                new QueryWrapper<PartyMembership>().eq("member_id", memberId).last("limit 1"));
        if (m == null || m.getCurrentStatus() == null) {
            return NORMAL;
        }
        return m.getCurrentStatus();
    }

    /** 党籍台账：党员 + 党籍状态 + 在办变动。 */
    public List<Map<String, Object>> ledger(Long orgId, String status, String keyword) {
        QueryWrapper<PartyMember> qw = new QueryWrapper<>();
        if (orgId != null) {
            qw.eq("org_id", orgId);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like("name", keyword).or().like("id_card", keyword));
        }
        qw.orderByAsc("org_id", "id");
        List<PartyMember> members = memberMapper.selectList(qw);

        List<Long> memberIds = members.stream().map(PartyMember::getId).collect(Collectors.toList());
        Map<Long, PartyMembership> msMap = new HashMap<>();
        Map<Long, MembershipChange> pendingMap = new HashMap<>();
        if (!memberIds.isEmpty()) {
            membershipMapper.selectList(new QueryWrapper<PartyMembership>().in("member_id", memberIds))
                    .forEach(m -> msMap.put(m.getMemberId(), m));
            changeMapper.selectList(new QueryWrapper<MembershipChange>()
                            .in("member_id", memberIds).eq("audit_status", "待审批")
                            .orderByDesc("create_time"))
                    .forEach(c -> pendingMap.putIfAbsent(c.getMemberId(), c));
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (PartyMember p : members) {
            PartyMembership ms = msMap.get(p.getId());
            String cur = ms != null && ms.getCurrentStatus() != null ? ms.getCurrentStatus() : NORMAL;
            if (status != null && !status.isBlank() && !status.equals(cur)) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("memberId", p.getId());
            row.put("name", p.getName());
            row.put("orgId", p.getOrgId());
            row.put("orgName", p.getOrgName());
            row.put("memberStatus", p.getMemberStatus());
            row.put("joinDate", p.getJoinDate());
            row.put("fullDate", p.getFullDate());
            row.put("currentStatus", cur);
            row.put("stopDate", ms != null ? ms.getStopDate() : null);
            row.put("restoreDate", ms != null ? ms.getRestoreDate() : null);
            MembershipChange pc = pendingMap.get(p.getId());
            row.put("pendingChange", pc != null ? pc.getChangeType() : null);
            row.put("pendingChangeId", pc != null ? pc.getId() : null);
            rows.add(row);
        }
        return rows;
    }

    /** 变动记录（可按人/组织过滤）。 */
    public List<MembershipChange> changes(Long memberId, Long orgId) {
        QueryWrapper<MembershipChange> qw = new QueryWrapper<>();
        if (memberId != null) {
            qw.eq("member_id", memberId);
        }
        if (orgId != null) {
            qw.eq("org_id", orgId);
        }
        qw.orderByDesc("create_time");
        return changeMapper.selectList(qw);
    }

    /** 发起党籍变动（不生效，待上级党委审批）。 */
    @Transactional
    public MembershipChange createChange(MembershipChange body) {
        if (body.getMemberId() == null) {
            throw PartyBizException.block("E-MS-02", "请选择党籍变动对象（党员）");
        }
        if (body.getChangeType() == null || body.getChangeType().isBlank()) {
            throw PartyBizException.block("E-MS-02", "变动类型必填");
        }
        if (body.getReason() == null || body.getReason().isBlank()) {
            throw PartyBizException.block("E-MS-02", "变动原因为必填项，不得为空");
        }
        if (body.getEffectiveDate() == null) {
            throw PartyBizException.block("E-MS-02", "变动生效时间必填");
        }
        PartyMember member = memberMapper.selectById(body.getMemberId());
        if (member == null) {
            throw PartyBizException.block("E-MS-02", "党员不存在");
        }

        String cur = currentStatus(member.getId());
        List<String> allowed = TRANSITIONS.getOrDefault(cur, List.of());
        if (!allowed.contains(body.getChangeType())) {
            throw PartyBizException.block("E-MS-01",
                    "党籍状态机非法流转：当前状态[" + cur + "]不允许[" + body.getChangeType() + "]");
        }
        Long pending = changeMapper.selectCount(new QueryWrapper<MembershipChange>()
                .eq("member_id", member.getId()).eq("audit_status", "待审批"));
        if (pending != null && pending > 0) {
            throw PartyBizException.block("E-MS-01", "该党员存在待审批的党籍变动，不得重复发起");
        }

        body.setId(null);
        body.setMemberName(member.getName());
        body.setOrgId(member.getOrgId());
        body.setOrgName(member.getOrgName());
        body.setAuditStatus("待审批");
        body.setSource(body.getSource() == null ? "MANUAL" : body.getSource());
        body.setCreator(CurrentHeader.operator());
        body.setCreateTime(LocalDateTime.now());
        body.setUpdateTime(LocalDateTime.now());
        changeMapper.insert(body);
        auditService.log("党籍变动发起", "MembershipChange", body.getId(),
                member.getName() + " " + body.getChangeType() + "，待上级党委审批");
        return body;
    }

    /** 上级党委审批；批准后按变动类型回写台账。 */
    @Transactional
    public MembershipChange audit(Long id, boolean pass, String opinion) {
        MembershipChange ch = changeMapper.selectById(id);
        if (ch == null) {
            throw PartyBizException.block("E-MS-01", "变动记录不存在");
        }
        if (!"待审批".equals(ch.getAuditStatus())) {
            throw PartyBizException.block("E-MS-01", "该变动已审批，不得重复审批");
        }
        ch.setAuditStatus(pass ? "已批准" : "已驳回");
        ch.setAuditOpinion(opinion);
        ch.setApprover(CurrentHeader.operator());
        ch.setApproveDate(LocalDate.now());
        ch.setUpdateTime(LocalDateTime.now());
        changeMapper.updateById(ch);

        if (pass) {
            applyChange(ch);
        }
        auditService.log(pass ? "党籍变动批准" : "党籍变动驳回", "MembershipChange", id,
                ch.getMemberName() + " " + ch.getChangeType() + (opinion == null ? "" : "：" + opinion));
        return ch;
    }

    /** 生效：回写 party_membership 与 party_member（审批通过才执行，E-MS-03）。 */
    private void applyChange(MembershipChange ch) {
        PartyMember member = memberMapper.selectById(ch.getMemberId());
        PartyMembership ms = membershipMapper.selectOne(
                new QueryWrapper<PartyMembership>().eq("member_id", ch.getMemberId()).last("limit 1"));
        boolean isNew = ms == null;
        if (isNew) {
            ms = new PartyMembership();
            ms.setMemberId(ch.getMemberId());
            ms.setMemberName(ch.getMemberName());
            ms.setJoinDate(member != null ? member.getJoinDate() : null);
            ms.setMembershipStatus(member != null ? member.getMemberStatus() : null);
        }

        String type = ch.getChangeType();
        String memberStatusAfter;
        switch (type) {
            case STOP:
                ms.setCurrentStatus(STOP);
                ms.setStopDate(ch.getEffectiveDate());
                memberStatusAfter = STOP;
                break;
            case RESTORE:
                ms.setCurrentStatus(NORMAL);
                ms.setRestoreDate(ch.getEffectiveDate());
                boolean probationary = member != null && (member.getFullDate() == null);
                memberStatusAfter = probationary ? "预备党员" : "正式党员";
                break;
            case TRANSFER_OUT:
                ms.setCurrentStatus(TRANSFER_OUT);
                memberStatusAfter = TRANSFER_OUT;
                break;
            case TRANSFER_IN:
                ms.setCurrentStatus(NORMAL);
                memberStatusAfter = member != null && member.getFullDate() == null ? "预备党员" : "正式党员";
                break;
            case DESERTER:
            case REMOVE:
            case EXPEL:
            case DEAD:
                ms.setCurrentStatus(type);
                memberStatusAfter = type;
                break;
            default:
                throw PartyBizException.block("E-MS-01", "未知变动类型：" + type);
        }
        ms.setStatusChangeDate(ch.getEffectiveDate());
        ms.setReason(ch.getReason());
        if (isNew) {
            membershipMapper.insert(ms);
        } else {
            membershipMapper.updateById(ms);
        }
        if (member != null) {
            member.setMemberStatus(memberStatusAfter);
            memberMapper.updateById(member);
        }
    }

    /** 开具党籍证明（自动编号 DZ+日期+3 位流水）。 */
    @Transactional
    public MembershipProof issueProof(MembershipProof body) {
        if (body.getMemberId() == null) {
            throw PartyBizException.block("E-MS-04", "请选择党员");
        }
        PartyMember member = memberMapper.selectById(body.getMemberId());
        if (member == null) {
            throw PartyBizException.block("E-MS-04", "党员不存在");
        }
        String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String prefix = "DZ" + today;
        Long cnt = proofMapper.selectCount(new QueryWrapper<MembershipProof>().likeRight("proof_no", prefix));
        String no;
        for (int i = (cnt == null ? 0 : cnt.intValue()) + 1; ; i++) {
            no = prefix + String.format("%03d", i);
            Long exists = proofMapper.selectCount(new QueryWrapper<MembershipProof>().eq("proof_no", no));
            if (exists == null || exists == 0) {
                break;
            }
        }
        body.setId(null);
        body.setMemberName(member.getName());
        body.setProofNo(no);
        body.setIssueDate(body.getIssueDate() == null ? LocalDate.now() : body.getIssueDate());
        body.setIssuer(CurrentHeader.operator());
        body.setCreateTime(LocalDateTime.now());
        proofMapper.insert(body);
        auditService.log("党籍证明开具", "MembershipProof", body.getId(),
                member.getName() + " " + no + (body.getPurpose() == null ? "" : " 用途:" + body.getPurpose()));
        return body;
    }

    public List<MembershipProof> proofList(Long memberId) {
        QueryWrapper<MembershipProof> qw = new QueryWrapper<>();
        if (memberId != null) {
            qw.eq("member_id", memberId);
        }
        qw.orderByDesc("create_time");
        return proofMapper.selectList(qw);
    }

    /** 状态机字典（供前端渲染）。 */
    public Map<String, Object> dict() {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("changeTypes", Arrays.asList(STOP, RESTORE, DESERTER, REMOVE, EXPEL, DEAD, TRANSFER_IN, TRANSFER_OUT));
        d.put("transitions", TRANSITIONS);
        d.put("terminal", new ArrayList<>(TERMINAL));
        d.put("statuses", Arrays.asList(NORMAL, STOP, TRANSFER_OUT, DESERTER, REMOVE, EXPEL, DEAD));
        return d;
    }
}
