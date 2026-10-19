package com.example.partyaffairsmgt.member.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.member.entity.RelationTransfer;
import com.example.partyaffairsmgt.member.entity.TransferFlow;
import com.example.partyaffairsmgt.member.entity.TransferLetter;
import com.example.partyaffairsmgt.member.service.RelationTransferService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 组织关系转接控制器（需求 6.2）。
 */
@RestController
@RequestMapping("/api/party/member/relation-transfer")
@CrossOrigin(origins = "*")
public class RelationTransferController {

    @Autowired
    private RelationTransferService transferService;

    /** 转接列表 */
    @GetMapping("/list")
    public ResponseResult<List<RelationTransfer>> list(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ResponseResult.success(transferService.list(orgId, status, keyword));
    }

    /** 发起转接申请 */
    @PostMapping
    public ResponseResult<RelationTransfer> apply(@RequestBody RelationTransfer transfer) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(transferService.apply(transfer));
    }

    /** 支部审核 */
    @PostMapping("/{id}/branch-audit")
    public ResponseResult<Void> branchAudit(
            @PathVariable Long id,
            @RequestParam boolean pass,
            @RequestParam(required = false) String opinion) {
        PartyRole.requireAny(PartyRole.R07, PartyRole.R08);
        transferService.branchAudit(id, pass, opinion);
        return ResponseResult.success(null);
    }

    /** 党委审批 */
    @PostMapping("/{id}/committee-audit")
    public ResponseResult<Void> committeeAudit(
            @PathVariable Long id,
            @RequestParam boolean pass,
            @RequestParam(required = false) String opinion) {
        PartyRole.requireAny(PartyRole.R03);
        transferService.committeeAudit(id, pass, opinion);
        return ResponseResult.success(null);
    }

    /** 开具介绍信 */
    @PostMapping("/{id}/issue-letter")
    public ResponseResult<TransferLetter> issueLetter(
            @PathVariable Long id,
            @RequestParam(required = false) Integer validDays) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(transferService.issueLetter(id, validDays));
    }

    /** 接收确认 */
    @PostMapping("/{id}/receive")
    public ResponseResult<Void> receive(@PathVariable Long id, @RequestParam String receiver) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        transferService.receive(id, receiver);
        return ResponseResult.success(null);
    }

    /** 回执归档 */
    @PostMapping("/{id}/receipt")
    public ResponseResult<Void> receipt(@PathVariable Long id, @RequestParam String receiptNo) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        transferService.receipt(id, receiptNo);
        return ResponseResult.success(null);
    }

    /** 超期未接收预警 */
    @GetMapping("/alerts")
    public ResponseResult<List<Map<String, Object>>> alerts() {
        return ResponseResult.success(transferService.alerts());
    }

    /** 介绍信列表 */
    @GetMapping("/letters")
    public ResponseResult<List<TransferLetter>> letters(@RequestParam(required = false) String status) {
        return ResponseResult.success(transferService.letters(status));
    }

    /** 介绍信详情 */
    @GetMapping("/letter/{letterNo}")
    public ResponseResult<TransferLetter> letterDetail(@PathVariable String letterNo) {
        return ResponseResult.success(transferService.letterDetail(letterNo));
    }

    /** 个人转接历史 */
    @GetMapping("/history")
    public ResponseResult<List<RelationTransfer>> history(@RequestParam Long memberId) {
        return ResponseResult.success(transferService.history(memberId));
    }

    /** 审批链记录 */
    @GetMapping("/{id}/flows")
    public ResponseResult<List<TransferFlow>> flows(@PathVariable Long id) {
        return ResponseResult.success(transferService.flows(id));
    }
}
