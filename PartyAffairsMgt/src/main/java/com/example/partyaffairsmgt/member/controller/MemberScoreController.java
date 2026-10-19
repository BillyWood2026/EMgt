package com.example.partyaffairsmgt.member.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.member.entity.ScoreFlow;
import com.example.partyaffairsmgt.member.entity.ScoreRule;
import com.example.partyaffairsmgt.member.service.MemberScoreService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 党员积分控制器（需求 6.3）。
 */
@RestController
@RequestMapping("/api/party/member/score")
@CrossOrigin(origins = "*")
public class MemberScoreController {

    @Autowired
    private MemberScoreService scoreService;

    /** 积分规则列表 */
    @GetMapping("/rules")
    public ResponseResult<List<ScoreRule>> rules() {
        return ResponseResult.success(scoreService.rules());
    }

    /** 保存积分规则 */
    @PostMapping("/rules")
    public ResponseResult<ScoreRule> saveRule(@RequestBody ScoreRule rule) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(scoreService.saveRule(rule));
    }

    /** 积分流水列表 */
    @GetMapping("/flows")
    public ResponseResult<List<ScoreFlow>> flows(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String ruleKey) {
        return ResponseResult.success(scoreService.flows(memberId, orgId, year, month, ruleKey));
    }

    /** 手工登记积分 */
    @PostMapping("/flows")
    public ResponseResult<ScoreFlow> addFlow(@RequestBody ScoreFlow flow) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(scoreService.addFlow(flow));
    }

    /** 手工加分审批 */
    @PostMapping("/flows/{id}/audit")
    public ResponseResult<Void> auditFlow(
            @PathVariable Long id,
            @RequestParam boolean pass,
            @RequestParam(required = false) String opinion) {
        PartyRole.requireAny(PartyRole.R03);
        scoreService.auditFlow(id, pass, opinion);
        return ResponseResult.success(null);
    }

    /** 积分排名 */
    @GetMapping("/rank")
    public ResponseResult<Map<String, Object>> rank(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "branch") String scope) {
        return ResponseResult.success(scoreService.rank(orgId, year, scope));
    }

    /** 个人积分明细 */
    @GetMapping("/my-score")
    public ResponseResult<Map<String, Object>> myScore(
            @RequestParam Long memberId,
            @RequestParam(required = false) Integer year) {
        return ResponseResult.success(scoreService.myScore(memberId, year));
    }

    /** 年度重置 */
    @PostMapping("/reset-year")
    public ResponseResult<Void> resetYear(
            @RequestParam Integer year,
            @RequestParam(defaultValue = "carryover") String mode) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        scoreService.resetYear(year, mode);
        return ResponseResult.success(null);
    }
}
