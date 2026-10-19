package com.example.partyaffairsmgt.member.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.member.entity.CareDifficulty;
import com.example.partyaffairsmgt.member.entity.CareHelp;
import com.example.partyaffairsmgt.member.entity.CareVisit;
import com.example.partyaffairsmgt.member.service.CareAssistanceService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 党员关怀帮扶控制器（需求 6.5）。
 */
@RestController
@RequestMapping("/api/party/member/care")
@CrossOrigin(origins = "*")
public class CareAssistanceController {

    @Autowired
    private CareAssistanceService careService;

    /** 困难台账列表 */
    @GetMapping("/difficulties")
    public ResponseResult<List<CareDifficulty>> difficulties(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String difficultyType) {
        return ResponseResult.success(careService.difficulties(orgId, difficultyType));
    }

    /** 新增困难台账 */
    @PostMapping("/difficulties")
    public ResponseResult<CareDifficulty> addDifficulty(@RequestBody CareDifficulty difficulty) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(careService.addDifficulty(difficulty));
    }

    /** 退出困难台账 */
    @PostMapping("/difficulties/{id}/exit")
    public ResponseResult<Void> exitDifficulty(@PathVariable Long id, @RequestParam String exitReason) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        careService.exitDifficulty(id, exitReason);
        return ResponseResult.success(null);
    }

    /** 走访慰问列表 */
    @GetMapping("/visits")
    public ResponseResult<List<CareVisit>> visits(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String visitNode,
            @RequestParam(required = false) Integer year) {
        return ResponseResult.success(careService.visits(orgId, visitNode, year));
    }

    /** 新增走访慰问 */
    @PostMapping("/visits")
    public ResponseResult<CareVisit> addVisit(@RequestBody CareVisit visit) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(careService.addVisit(visit));
    }

    /** 帮扶申请列表 */
    @GetMapping("/helps")
    public ResponseResult<List<CareHelp>> helps(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String status) {
        return ResponseResult.success(careService.helps(orgId, status));
    }

    /** 新增帮扶申请 */
    @PostMapping("/helps")
    public ResponseResult<CareHelp> addHelp(@RequestBody CareHelp help) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(careService.addHelp(help));
    }

    /** 帮扶审批 */
    @PostMapping("/helps/{id}/approve")
    public ResponseResult<Void> approveHelp(
            @PathVariable Long id,
            @RequestParam boolean pass,
            @RequestParam(required = false) String opinion) {
        PartyRole.requireAny(PartyRole.R03);
        careService.approveHelp(id, pass, opinion);
        return ResponseResult.success(null);
    }

    /** 帮扶发放 */
    @PostMapping("/helps/{id}/grant")
    public ResponseResult<Void> grantHelp(
            @PathVariable Long id,
            @RequestParam String grantWay,
            @RequestParam(required = false) Long duesExpenseId) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        careService.grantHelp(id, grantWay, duesExpenseId);
        return ResponseResult.success(null);
    }

    /** 帮扶名单公示合规校验 */
    @GetMapping("/publicity-check")
    public ResponseResult<List<Map<String, Object>>> publicityCheck() {
        return ResponseResult.success(careService.publicityCheck());
    }
}
