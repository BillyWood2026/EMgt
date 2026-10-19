package com.example.partyaffairsmgt.member.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.member.entity.PartyMember;
import com.example.partyaffairsmgt.member.entity.RelationTransfer;
import com.example.partyaffairsmgt.member.entity.TransferFlow;
import com.example.partyaffairsmgt.member.entity.TransferLetter;
import com.example.partyaffairsmgt.member.mapper.PartyMemberMapper;
import com.example.partyaffairsmgt.member.mapper.RelationTransferMapper;
import com.example.partyaffairsmgt.member.mapper.TransferFlowMapper;
import com.example.partyaffairsmgt.member.mapper.TransferLetterMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 组织关系转接服务（需求 6.2）。
 * 流程：申请→支部审核→党委审批→开具介绍信→接收确认→回执归档。
 */
@Service
public class RelationTransferService {

    @Autowired
    private RelationTransferMapper transferMapper;
    @Autowired
    private TransferFlowMapper flowMapper;
    @Autowired
    private TransferLetterMapper letterMapper;
    @Autowired
    private PartyMemberMapper memberMapper;
    @Autowired
    private AuditService auditService;

    /** 转接列表 */
    public List<RelationTransfer> list(Long orgId, String status, String keyword) {
        QueryWrapper<RelationTransfer> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (orgId != null) {
            qw.and(q -> q.eq("from_org_id", orgId).or().eq("to_org_id", orgId));
        }
        if (status != null && !status.isBlank()) qw.eq("flow_status", status);
        if (keyword != null && !keyword.isBlank()) {
            qw.and(q -> q.like("member_name", keyword).or().like("letter_no", keyword));
        }
        return transferMapper.selectList(qw);
    }

    /** 发起转接申请 */
    @Transactional
    public RelationTransfer apply(RelationTransfer transfer) {
        // 校验党员存在且属于原组织
        PartyMember member = memberMapper.selectById(transfer.getMemberId());
        if (member == null || member.getDeleted() == 1) {
            throw PartyBizException.block("E-MB-06", "转接对象不存在");
        }
        if (!member.getOrgId().equals(transfer.getFromOrgId())) {
            throw PartyBizException.block("E-MB-06", "转接对象非本组织党员");
        }
        // 检查在办流程
        Long count = transferMapper.selectCount(
                new QueryWrapper<RelationTransfer>()
                        .eq("member_id", transfer.getMemberId())
                        .eq("flow_status", "在办")
                        .eq("deleted", 0));
        if (count > 0) {
            throw PartyBizException.block("E-MB-07", "存在未完结的在办转接流程，禁止重复申请");
        }
        // 预备党员材料不完整预警
        if ("预备党员".equals(member.getPoliticalStatus())) {
            // TODO: 检查预备党员材料完整性
            auditService.log("组织关系转接", "预警", transfer.getMemberId(), "预备党员转接，请检查材料完整性");
        }
        transfer.setApplyDate(LocalDate.now());
        transfer.setStatus("在办");
        transfer.setFlowStatus("在办");
        transfer.setCurrentNode("支部审核");
        transfer.setCreateTime(LocalDateTime.now());
        transferMapper.insert(transfer);
        // 记录流程
        saveFlow(transfer.getId(), "申请", CurrentHeader.operator(), "发起转接申请", "通过");
        auditService.log("组织关系转接", "申请", transfer.getId(), "党员:" + transfer.getMemberName());
        return transfer;
    }

    /** 支部审核 */
    @Transactional
    public void branchAudit(Long transferId, boolean pass, String opinion) {
        RelationTransfer t = getTransfer(transferId);
        if (!"支部审核".equals(t.getCurrentNode())) {
            throw PartyBizException.block("E-MB-08", "当前节点不是支部审核");
        }
        saveFlow(transferId, "支部审核", CurrentHeader.operator(), opinion, pass ? "通过" : "驳回");
        if (pass) {
            t.setCurrentNode("党委审批");
        } else {
            t.setFlowStatus("已驳回");
            t.setStatus("已驳回");
        }
        t.setUpdateTime(LocalDateTime.now());
        transferMapper.updateById(t);
        auditService.log("组织关系转接", "支部审核", transferId, pass ? "通过" : "驳回");
    }

    /** 党委审批 */
    @Transactional
    public void committeeAudit(Long transferId, boolean pass, String opinion) {
        RelationTransfer t = getTransfer(transferId);
        if (!"党委审批".equals(t.getCurrentNode())) {
            throw PartyBizException.block("E-MB-08", "当前节点不是党委审批");
        }
        saveFlow(transferId, "党委审批", CurrentHeader.operator(), opinion, pass ? "通过" : "驳回");
        if (pass) {
            t.setCurrentNode("开具介绍信");
        } else {
            t.setFlowStatus("已驳回");
            t.setStatus("已驳回");
        }
        t.setUpdateTime(LocalDateTime.now());
        transferMapper.updateById(t);
        auditService.log("组织关系转接", "党委审批", transferId, pass ? "通过" : "驳回");
    }

    /** 开具介绍信 */
    @Transactional
    public TransferLetter issueLetter(Long transferId, Integer validDays) {
        RelationTransfer t = getTransfer(transferId);
        if (!"开具介绍信".equals(t.getCurrentNode())) {
            throw PartyBizException.block("E-MB-08", "当前节点不是开具介绍信");
        }
        if (validDays == null || validDays <= 0) {
            validDays = 30;
        }
        // 生成介绍信编号：组织编码+年份+4位流水
        String letterNo = generateLetterNo(t.getFromOrgId());
        TransferLetter letter = new TransferLetter();
        letter.setLetterNo(letterNo);
        letter.setTransferId(transferId);
        letter.setMemberId(t.getMemberId());
        letter.setMemberName(t.getMemberName());
        letter.setFromOrgId(t.getFromOrgId());
        letter.setFromOrgName(t.getFromOrg());
        letter.setToOrgId(t.getToOrgId());
        letter.setToOrgName(t.getToOrg());
        letter.setIssueDate(LocalDate.now());
        letter.setValidDays(validDays);
        letter.setExpireDate(LocalDate.now().plusDays(validDays));
        letter.setStatus("已开具");
        letter.setCreateTime(LocalDateTime.now());
        letterMapper.insert(letter);
        // 更新转接记录
        t.setLetterNo(letterNo);
        t.setCurrentNode("接收确认");
        t.setUpdateTime(LocalDateTime.now());
        transferMapper.updateById(t);
        saveFlow(transferId, "开具介绍信", CurrentHeader.operator(), "介绍信编号:" + letterNo, "通过");
        auditService.log("组织关系转接", "开具介绍信", transferId, "编号:" + letterNo);
        return letter;
    }

    /** 接收确认 */
    @Transactional
    public void receive(Long transferId, String receiver) {
        RelationTransfer t = getTransfer(transferId);
        if (!"接收确认".equals(t.getCurrentNode())) {
            throw PartyBizException.block("E-MB-08", "当前节点不是接收确认");
        }
        t.setCurrentNode("回执归档");
        t.setReceiveDate(LocalDate.now());
        t.setUpdateTime(LocalDateTime.now());
        transferMapper.updateById(t);
        // 更新介绍信状态
        TransferLetter letter = letterMapper.selectOne(
                new QueryWrapper<TransferLetter>().eq("transfer_id", transferId).eq("deleted", 0));
        if (letter != null) {
            letter.setStatus("已接收");
            letter.setReceiveDate(LocalDate.now());
            letter.setReceiver(receiver);
            letter.setUpdateTime(LocalDateTime.now());
            letterMapper.updateById(letter);
        }
        // 更新党员组织
        PartyMember member = memberMapper.selectById(t.getMemberId());
        if (member != null && t.getToOrgId() != null) {
            member.setOrgId(t.getToOrgId());
            member.setOrgName(t.getToOrg());
            member.setUpdateTime(LocalDateTime.now());
            memberMapper.updateById(member);
        }
        saveFlow(transferId, "接收确认", receiver, "接收党员", "通过");
        auditService.log("组织关系转接", "接收确认", transferId, "接收人:" + receiver);
    }

    /** 回执归档 */
    @Transactional
    public void receipt(Long transferId, String receiptNo) {
        RelationTransfer t = getTransfer(transferId);
        if (!"回执归档".equals(t.getCurrentNode())) {
            throw PartyBizException.block("E-MB-08", "当前节点不是回执归档");
        }
        t.setCurrentNode("已完成");
        t.setFlowStatus("已完成");
        t.setStatus("已完成");
        t.setReceiptStatus("已回执");
        t.setUpdateTime(LocalDateTime.now());
        transferMapper.updateById(t);
        // 更新介绍信
        TransferLetter letter = letterMapper.selectOne(
                new QueryWrapper<TransferLetter>().eq("transfer_id", transferId).eq("deleted", 0));
        if (letter != null) {
            letter.setStatus("已回执");
            letter.setReceiptNo(receiptNo);
            letter.setReceiptDate(LocalDate.now());
            letter.setUpdateTime(LocalDateTime.now());
            letterMapper.updateById(letter);
        }
        saveFlow(transferId, "回执归档", CurrentHeader.operator(), "回执编号:" + receiptNo, "通过");
        auditService.log("组织关系转接", "回执归档", transferId, "回执编号:" + receiptNo);
    }

    /** 超期未接收预警 */
    public List<Map<String, Object>> alerts() {
        // 查找已开具介绍信但超期未接收的
        LocalDate today = LocalDate.now();
        List<TransferLetter> letters = letterMapper.selectList(
                new QueryWrapper<TransferLetter>()
                        .eq("status", "已开具")
                        .lt("expire_date", today)
                        .eq("deleted", 0));
        return letters.stream().map(l -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("letterNo", l.getLetterNo());
            map.put("memberName", l.getMemberName());
            map.put("fromOrg", l.getFromOrgName());
            map.put("toOrg", l.getToOrgName());
            map.put("expireDate", l.getExpireDate());
            long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(l.getExpireDate(), today);
            map.put("overdueDays", overdueDays);
            map.put("alertLevel", overdueDays >= 30 ? "告警" : "预警");
            return map;
        }).collect(Collectors.toList());
    }

    /** 介绍信列表 */
    public List<TransferLetter> letters(String status) {
        QueryWrapper<TransferLetter> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (status != null && !status.isBlank()) qw.eq("status", status);
        return letterMapper.selectList(qw);
    }

    /** 介绍信详情 */
    public TransferLetter letterDetail(String letterNo) {
        return letterMapper.selectOne(
                new QueryWrapper<TransferLetter>().eq("letter_no", letterNo).eq("deleted", 0));
    }

    /** 个人转接历史 */
    public List<RelationTransfer> history(Long memberId) {
        return transferMapper.selectList(
                new QueryWrapper<RelationTransfer>()
                        .eq("member_id", memberId)
                        .eq("deleted", 0)
                        .orderByDesc("create_time"));
    }

    /** 审批链记录 */
    public List<TransferFlow> flows(Long transferId) {
        return flowMapper.selectList(
                new QueryWrapper<TransferFlow>()
                        .eq("transfer_id", transferId)
                        .orderByAsc("create_time"));
    }

    private RelationTransfer getTransfer(Long id) {
        RelationTransfer t = transferMapper.selectById(id);
        if (t == null || t.getDeleted() == 1) {
            throw PartyBizException.block("E-MB-08", "转接记录不存在");
        }
        return t;
    }

    private void saveFlow(Long transferId, String node, String approver, String opinion, String status) {
        TransferFlow flow = new TransferFlow();
        flow.setTransferId(transferId);
        flow.setNode(node);
        flow.setApprover(approver);
        flow.setOpinion(opinion);
        flow.setAuditTime(LocalDateTime.now());
        flow.setStatus(status);
        flow.setCreateTime(LocalDateTime.now());
        flowMapper.insert(flow);
    }

    private String generateLetterNo(Long fromOrgId) {
        // 组织编码+年份+4位流水
        String orgCode = fromOrgId == null ? "000" : String.valueOf(fromOrgId);
        String year = String.valueOf(LocalDate.now().getYear());
        // 查询当年最大流水
        QueryWrapper<TransferLetter> qw = new QueryWrapper<>();
        qw.likeRight("letter_no", orgCode + year)
          .orderByDesc("letter_no")
          .last("limit 1");
        TransferLetter last = letterMapper.selectOne(qw);
        int seq = 1;
        if (last != null && last.getLetterNo() != null && last.getLetterNo().length() >= 4) {
            String seqStr = last.getLetterNo().substring(last.getLetterNo().length() - 4);
            try {
                seq = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                seq = 1;
            }
        }
        return orgCode + year + String.format("%04d", seq);
    }
}
