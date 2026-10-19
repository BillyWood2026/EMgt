package com.example.strategymgt.workplan.controller;

import com.example.strategymgt.common.ResponseResult;
import com.example.strategymgt.workplan.entity.*;
import com.example.strategymgt.workplan.service.WorkplanService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/strategy/workplan")
public class WorkplanController {

    private final WorkplanService workplanService;

    public WorkplanController(WorkplanService workplanService) {
        this.workplanService = workplanService;
    }

    // ---- 模板配置 ----
    @GetMapping("/templates")
    public ResponseResult<List<WorkplanTemplate>> templates() { return ResponseResult.ok(workplanService.listTemplates()); }
    @PostMapping("/template")
    public ResponseResult<WorkplanTemplate> saveTemplate(@RequestBody WorkplanTemplate t) { return ResponseResult.ok(workplanService.saveTemplate(t)); }
    @PutMapping("/template/{id}")
    public ResponseResult<Boolean> updateTemplate(@PathVariable Long id, @RequestBody WorkplanTemplate t) {
        t.setId(id);
        return ResponseResult.ok(workplanService.updateTemplate(t));
    }
    @DeleteMapping("/template/{id}")
    public ResponseResult<Boolean> deleteTemplate(@PathVariable Long id) { return ResponseResult.ok(workplanService.deleteTemplate(id)); }

    // ---- 计划主表 ----
    @GetMapping("/plans")
    public ResponseResult<List<WorkplanMain>> plans(@RequestParam(required = false) Integer year,
                                                    @RequestParam(required = false) String status) {
        return ResponseResult.ok(workplanService.listPlans(year, status));
    }
    @PostMapping("/plan")
    public ResponseResult<WorkplanMain> savePlan(@RequestBody WorkplanMain p) { return ResponseResult.ok(workplanService.savePlan(p)); }
    @PutMapping("/plan/{id}")
    public ResponseResult<Boolean> updatePlan(@PathVariable Long id, @RequestBody WorkplanMain p) {
        p.setId(id);
        return ResponseResult.ok(workplanService.updatePlan(p));
    }
    @DeleteMapping("/plan/{id}")
    public ResponseResult<Boolean> deletePlan(@PathVariable Long id) { return ResponseResult.ok(workplanService.deletePlan(id)); }
    @PostMapping("/plan/{id}/status")
    public ResponseResult<WorkplanMain> changeStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseResult.ok(workplanService.changeStatus(id, status));
    }
    @PostMapping("/plan/{id}/lock")
    public ResponseResult<WorkplanMain> lockPlan(@PathVariable Long id) { return ResponseResult.ok(workplanService.lockPlan(id)); }

    // ---- WBS任务 ----
    @GetMapping("/plan/{planId}/wbs")
    public ResponseResult<List<WorkplanWbsTask>> wbs(@PathVariable Long planId) {
        return ResponseResult.ok(workplanService.listWbsTasks(planId));
    }
    @PostMapping("/wbs")
    public ResponseResult<WorkplanWbsTask> saveWbs(@RequestBody WorkplanWbsTask t) { return ResponseResult.ok(workplanService.saveWbsTask(t)); }
    @PutMapping("/wbs/{id}")
    public ResponseResult<Boolean> updateWbs(@PathVariable Long id, @RequestBody WorkplanWbsTask t) {
        t.setId(id);
        return ResponseResult.ok(workplanService.updateWbsTask(t));
    }
    @DeleteMapping("/wbs/{id}")
    public ResponseResult<Boolean> deleteWbs(@PathVariable Long id) { return ResponseResult.ok(workplanService.deleteWbsTask(id)); }
    @GetMapping("/plan/{planId}/wbs/check-weight")
    public ResponseResult<Map<String, Object>> checkWeight(@PathVariable Long planId) {
        return ResponseResult.ok(workplanService.checkWbsWeight(planId));
    }
    @PostMapping("/wbs/{id}/complete")
    public ResponseResult<WorkplanWbsTask> completeTask(@PathVariable Long id) {
        return ResponseResult.ok(workplanService.completeTask(id));
    }

    // ---- 审批 ----
    @GetMapping("/approvals")
    public ResponseResult<List<WorkplanApproval>> approvals(@RequestParam(required = false) Long planId) {
        return ResponseResult.ok(workplanService.listApprovals(planId));
    }
    @PostMapping("/approval")
    public ResponseResult<WorkplanApproval> saveApproval(@RequestBody WorkplanApproval a) {
        return ResponseResult.ok(workplanService.saveApproval(a));
    }
    @PutMapping("/approval/{id}")
    public ResponseResult<Boolean> updateApproval(@PathVariable Long id, @RequestBody WorkplanApproval a) {
        a.setId(id);
        return ResponseResult.ok(workplanService.updateApproval(a));
    }
    @DeleteMapping("/approval/{id}")
    public ResponseResult<Boolean> deleteApproval(@PathVariable Long id) { return ResponseResult.ok(workplanService.deleteApproval(id)); }

    // ---- 执行跟踪 ----
    @GetMapping("/tracks")
    public ResponseResult<List<WorkplanProgressTrack>> tracks(@RequestParam(required = false) Long planId) {
        return ResponseResult.ok(workplanService.listTracks(planId));
    }
    @PostMapping("/track")
    public ResponseResult<WorkplanProgressTrack> saveTrack(@RequestBody WorkplanProgressTrack t) {
        return ResponseResult.ok(workplanService.saveTrack(t));
    }
    @PutMapping("/track/{id}")
    public ResponseResult<Boolean> updateTrack(@PathVariable Long id, @RequestBody WorkplanProgressTrack t) {
        t.setId(id);
        return ResponseResult.ok(workplanService.updateTrack(t));
    }
    @DeleteMapping("/track/{id}")
    public ResponseResult<Boolean> deleteTrack(@PathVariable Long id) { return ResponseResult.ok(workplanService.deleteTrack(id)); }

    // ---- 总结复盘 ----
    @GetMapping("/summaries")
    public ResponseResult<List<WorkplanSummary>> summaries(@RequestParam(required = false) Long planId) {
        return ResponseResult.ok(workplanService.listSummaries(planId));
    }
    @PostMapping("/summary")
    public ResponseResult<WorkplanSummary> saveSummary(@RequestBody WorkplanSummary s) {
        return ResponseResult.ok(workplanService.saveSummary(s));
    }
    @PutMapping("/summary/{id}")
    public ResponseResult<Boolean> updateSummary(@PathVariable Long id, @RequestBody WorkplanSummary s) {
        s.setId(id);
        return ResponseResult.ok(workplanService.updateSummary(s));
    }
    @DeleteMapping("/summary/{id}")
    public ResponseResult<Boolean> deleteSummary(@PathVariable Long id) { return ResponseResult.ok(workplanService.deleteSummary(id)); }

    // ---- 考核打分 ----
    @GetMapping("/scores")
    public ResponseResult<List<WorkplanScore>> scores(@RequestParam(required = false) Long planId) {
        return ResponseResult.ok(workplanService.listScores(planId));
    }
    @PostMapping("/score")
    public ResponseResult<WorkplanScore> saveScore(@RequestBody WorkplanScore s) {
        return ResponseResult.ok(workplanService.saveScore(s));
    }
    @PutMapping("/score/{id}")
    public ResponseResult<Boolean> updateScore(@PathVariable Long id, @RequestBody WorkplanScore s) {
        s.setId(id);
        return ResponseResult.ok(workplanService.updateScore(s));
    }
    @DeleteMapping("/score/{id}")
    public ResponseResult<Boolean> deleteScore(@PathVariable Long id) { return ResponseResult.ok(workplanService.deleteScore(id)); }
    @GetMapping("/plan/{planId}/total-score")
    public ResponseResult<BigDecimal> totalScore(@PathVariable Long planId) {
        return ResponseResult.ok(workplanService.calcTotalScore(planId));
    }

    // ---- 台账统计 ----
    @GetMapping("/ledgers")
    public ResponseResult<List<WorkplanLedger>> ledgers() { return ResponseResult.ok(workplanService.listLedgers()); }
    @GetMapping("/plan/{planId}/completion-rate")
    public ResponseResult<BigDecimal> completionRate(@PathVariable Long planId) {
        return ResponseResult.ok(workplanService.calcPlanCompletionRate(planId));
    }
    @PostMapping("/plan/{planId}/ledger")
    public ResponseResult<WorkplanLedger> generateLedger(@PathVariable Long planId, @RequestParam String period) {
        return ResponseResult.ok(workplanService.generateLedger(planId, period));
    }
    @PutMapping("/ledger/{id}")
    public ResponseResult<Boolean> updateLedger(@PathVariable Long id, @RequestBody WorkplanLedger l) {
        l.setId(id);
        return ResponseResult.ok(workplanService.updateLedger(l));
    }
    @DeleteMapping("/ledger/{id}")
    public ResponseResult<Boolean> deleteLedger(@PathVariable Long id) { return ResponseResult.ok(workplanService.deleteLedger(id)); }
}
