package com.example.partyaffairsmgt.assessment.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.assessment.entity.AssessAppeal;
import com.example.partyaffairsmgt.assessment.entity.AssessCheck;
import com.example.partyaffairsmgt.assessment.entity.AssessScore;
import com.example.partyaffairsmgt.assessment.entity.AssessSelfEval;
import com.example.partyaffairsmgt.assessment.entity.AssessTask;
import com.example.partyaffairsmgt.assessment.service.AssessTaskService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 考核流程：自评/核查/评分/审定/申诉（需求 10.3）。
 */
@RestController
@RequestMapping("/api/party/assessment/process")
@CrossOrigin(origins = "*")
public class AssessProcessController {

    @Autowired
    private AssessTaskService taskService;

    @GetMapping("/task/list")
    public ResponseResult<List<AssessTask>> list(
            @RequestParam(required = false) Long schemeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long orgId) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(taskService.list(schemeId, status, orgId));
    }

    @GetMapping("/self-eval/list")
    public ResponseResult<List<AssessSelfEval>> selfEvalList(@RequestParam Long taskId) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(taskService.selfEvalList(taskId));
    }

    @PostMapping("/self-eval/save")
    public ResponseResult<AssessSelfEval> saveSelfEval(@RequestParam Long taskId, @RequestBody AssessSelfEval e) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07);
        return ResponseResult.success(taskService.saveSelfEval(taskId, e));
    }

    @PostMapping("/{taskId}/submit-self")
    public ResponseResult<AssessTask> submitSelf(@PathVariable Long taskId,
                                                  @RequestParam(required = false) String submitBy) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07);
        return ResponseResult.success(taskService.submitSelf(taskId, submitBy));
    }

    @GetMapping("/check/auto-fetch")
    public ResponseResult<Map<String, Object>> autoFetch(@RequestParam Long taskId,
                                                          @RequestParam Long indicatorId) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(taskService.autoFetch(taskId, indicatorId));
    }

    @PostMapping("/check/save")
    public ResponseResult<AssessCheck> saveCheck(@RequestParam Long taskId, @RequestBody AssessCheck c) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(taskService.saveCheck(taskId, c));
    }

    @PostMapping("/{taskId}/finish-check")
    public ResponseResult<AssessTask> finishCheck(@PathVariable Long taskId) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(taskService.finishCheck(taskId));
    }

    @PostMapping("/{taskId}/finish-score")
    public ResponseResult<AssessTask> finishScore(@PathVariable Long taskId,
                                                   @RequestBody List<AssessScore> scores,
                                                   @RequestParam(required = false) BigDecimal adjustScore) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(taskService.finishScore(taskId, scores, adjustScore));
    }

    @PostMapping("/{taskId}/approve")
    public ResponseResult<AssessTask> approve(@PathVariable Long taskId,
                                               @RequestParam String approver,
                                               @RequestParam(required = false) String opinion) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04);
        return ResponseResult.success(taskService.approve(taskId, approver, opinion));
    }

    @PostMapping("/{taskId}/appeal")
    public ResponseResult<AssessAppeal> submitAppeal(@PathVariable Long taskId, @RequestParam String reason) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07);
        return ResponseResult.success(taskService.submitAppeal(taskId, reason));
    }

    @PostMapping("/appeal/{appealId}/handle")
    public ResponseResult<AssessAppeal> handleAppeal(@PathVariable Long appealId,
                                                      @RequestParam boolean upheld,
                                                      @RequestParam(required = false) BigDecimal newScore,
                                                      @RequestParam String handleResult,
                                                      @RequestParam String handleBy) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04);
        return ResponseResult.success(taskService.handleAppeal(appealId, upheld, newScore, handleResult, handleBy));
    }

    @GetMapping("/appeal/list")
    public ResponseResult<List<AssessAppeal>> appealList(@RequestParam(required = false) Long taskId) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(taskService.appealList(taskId));
    }

    @GetMapping("/check/list")
    public ResponseResult<List<AssessCheck>> checkList(@RequestParam Long taskId) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(taskService.checkList(taskId));
    }

    @GetMapping("/score/list")
    public ResponseResult<List<AssessScore>> scoreList(@RequestParam Long taskId) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(taskService.scoreList(taskId));
    }

    @PostMapping("/appeal-timeout-check")
    public ResponseResult<Map<String, Object>> appealTimeoutCheck() {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04);
        return ResponseResult.success(Map.of("overdue", taskService.appealTimeoutCheck()));
    }
}
