package com.example.strategymgt.dste.controller;

import com.example.strategymgt.dste.entity.*;
import com.example.strategymgt.dste.service.DsteService;
import com.example.strategymgt.common.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/strategy/dste")
public class DsteController {

    private final DsteService dsteService;

    public DsteController(DsteService dsteService) {
        this.dsteService = dsteService;
    }

    // ---- 战略目标拆解 ----
    @GetMapping("/goals")
    public ResponseResult<List<DsteGoalBreakdown>> goals(@RequestParam(required = false) Long spId) {
        return ResponseResult.ok(dsteService.listGoals(spId));
    }
    @PostMapping("/goal")
    public ResponseResult<DsteGoalBreakdown> saveGoal(@RequestBody DsteGoalBreakdown goal) {
        return ResponseResult.ok(dsteService.saveGoal(goal));
    }
    @PutMapping("/goal/{id}")
    public ResponseResult<Boolean> updateGoal(@PathVariable Long id, @RequestBody DsteGoalBreakdown goal) {
        goal.setId(id);
        return ResponseResult.ok(dsteService.updateGoal(goal));
    }
    @DeleteMapping("/goal/{id}")
    public ResponseResult<Boolean> deleteGoal(@PathVariable Long id) {
        return ResponseResult.ok(dsteService.deleteGoal(id));
    }
    @GetMapping("/goal/{id}/alignment")
    public ResponseResult<Map<String, Object>> goalAlignment(@PathVariable Long id) {
        return ResponseResult.ok(dsteService.checkGoalAlignment(id));
    }

    // ---- CSF关键成功因素 ----
    @GetMapping("/csfs")
    public ResponseResult<List<DsteCsf>> csfs(@RequestParam(required = false) Long goalId) {
        return ResponseResult.ok(dsteService.listCsfs(goalId));
    }
    @PostMapping("/csf")
    public ResponseResult<DsteCsf> saveCsf(@RequestBody DsteCsf csf) {
        return ResponseResult.ok(dsteService.saveCsf(csf));
    }
    @PutMapping("/csf/{id}")
    public ResponseResult<Boolean> updateCsf(@PathVariable Long id, @RequestBody DsteCsf csf) {
        csf.setId(id);
        return ResponseResult.ok(dsteService.updateCsf(csf));
    }
    @DeleteMapping("/csf/{id}")
    public ResponseResult<Boolean> deleteCsf(@PathVariable Long id) {
        return ResponseResult.ok(dsteService.deleteCsf(id));
    }

    // ---- KPI指标库 ----
    @GetMapping("/kpis")
    public ResponseResult<List<DsteKpi>> kpis() {
        return ResponseResult.ok(dsteService.listKpis());
    }
    @PostMapping("/kpi")
    public ResponseResult<DsteKpi> saveKpi(@RequestBody DsteKpi kpi) {
        return ResponseResult.ok(dsteService.saveKpi(kpi));
    }
    @PutMapping("/kpi/{id}")
    public ResponseResult<Boolean> updateKpi(@PathVariable Long id, @RequestBody DsteKpi kpi) {
        kpi.setId(id);
        return ResponseResult.ok(dsteService.updateKpi(kpi));
    }
    @DeleteMapping("/kpi/{id}")
    public ResponseResult<Boolean> deleteKpi(@PathVariable Long id) {
        return ResponseResult.ok(dsteService.deleteKpi(id));
    }
    @GetMapping("/kpis/dim-stats")
    public ResponseResult<Map<String, Long>> kpiDimStats() {
        return ResponseResult.ok(dsteService.kpiDimStats());
    }

    // ---- SP/BP重点任务 ----
    @GetMapping("/tasks")
    public ResponseResult<List<DsteKeyTask>> tasks(@RequestParam(required = false) String taskLevel) {
        return ResponseResult.ok(dsteService.listTasks(taskLevel));
    }
    @PostMapping("/task")
    public ResponseResult<DsteKeyTask> saveTask(@RequestBody DsteKeyTask task) {
        return ResponseResult.ok(dsteService.saveTask(task));
    }
    @PutMapping("/task/{id}")
    public ResponseResult<Boolean> updateTask(@PathVariable Long id, @RequestBody DsteKeyTask task) {
        task.setId(id);
        return ResponseResult.ok(dsteService.updateTask(task));
    }
    @DeleteMapping("/task/{id}")
    public ResponseResult<Boolean> deleteTask(@PathVariable Long id) {
        return ResponseResult.ok(dsteService.deleteTask(id));
    }
    @PostMapping("/tasks/refresh-status")
    public ResponseResult<Void> refreshTaskStatus() {
        dsteService.refreshTaskStatus();
        return ResponseResult.ok();
    }

    // ---- 资源匹配 ----
    @GetMapping("/resources")
    public ResponseResult<List<DsteResourceMatch>> resources(@RequestParam(required = false) Long taskId) {
        return ResponseResult.ok(dsteService.listResources(taskId));
    }
    @PostMapping("/resource")
    public ResponseResult<DsteResourceMatch> saveResource(@RequestBody DsteResourceMatch r) {
        return ResponseResult.ok(dsteService.saveResource(r));
    }
    @PutMapping("/resource/{id}")
    public ResponseResult<Boolean> updateResource(@PathVariable Long id, @RequestBody DsteResourceMatch r) {
        r.setId(id);
        return ResponseResult.ok(dsteService.updateResource(r));
    }
    @DeleteMapping("/resource/{id}")
    public ResponseResult<Boolean> deleteResource(@PathVariable Long id) {
        return ResponseResult.ok(dsteService.deleteResource(id));
    }

    // ---- 责任体系台账 ----
    @GetMapping("/resp-ledgers")
    public ResponseResult<List<DsteResponsibilityLedger>> respLedgers() {
        return ResponseResult.ok(dsteService.listRespLedgers());
    }
    @PostMapping("/resp-ledger")
    public ResponseResult<DsteResponsibilityLedger> saveRespLedger(@RequestBody DsteResponsibilityLedger r) {
        return ResponseResult.ok(dsteService.saveRespLedger(r));
    }
    @PutMapping("/resp-ledger/{id}")
    public ResponseResult<Boolean> updateRespLedger(@PathVariable Long id, @RequestBody DsteResponsibilityLedger r) {
        r.setId(id);
        return ResponseResult.ok(dsteService.updateRespLedger(r));
    }
    @DeleteMapping("/resp-ledger/{id}")
    public ResponseResult<Boolean> deleteRespLedger(@PathVariable Long id) {
        return ResponseResult.ok(dsteService.deleteRespLedger(id));
    }

    // ---- IBP预算联动 ----
    @GetMapping("/ibp-budgets")
    public ResponseResult<List<DsteIbpBudget>> ibpBudgets() {
        return ResponseResult.ok(dsteService.listIbpBudgets());
    }
    @PostMapping("/ibp-budget")
    public ResponseResult<DsteIbpBudget> saveIbpBudget(@RequestBody DsteIbpBudget b) {
        return ResponseResult.ok(dsteService.saveIbpBudget(b));
    }
    @PutMapping("/ibp-budget/{id}")
    public ResponseResult<Boolean> updateIbpBudget(@PathVariable Long id, @RequestBody DsteIbpBudget b) {
        b.setId(id);
        return ResponseResult.ok(dsteService.updateIbpBudget(b));
    }
    @DeleteMapping("/ibp-budget/{id}")
    public ResponseResult<Boolean> deleteIbpBudget(@PathVariable Long id) {
        return ResponseResult.ok(dsteService.deleteIbpBudget(id));
    }

    // ---- 任务进度监控 ----
    @GetMapping("/progress")
    public ResponseResult<List<DsteTaskProgress>> progress(@RequestParam(required = false) Long taskId) {
        return ResponseResult.ok(dsteService.listProgress(taskId));
    }
    @PostMapping("/progress")
    public ResponseResult<DsteTaskProgress> saveProgress(@RequestBody DsteTaskProgress p) {
        return ResponseResult.ok(dsteService.saveProgress(p));
    }
    @PutMapping("/progress/{id}")
    public ResponseResult<Boolean> updateProgress(@PathVariable Long id, @RequestBody DsteTaskProgress p) {
        p.setId(id);
        return ResponseResult.ok(dsteService.updateProgress(p));
    }
    @DeleteMapping("/progress/{id}")
    public ResponseResult<Boolean> deleteProgress(@PathVariable Long id) {
        return ResponseResult.ok(dsteService.deleteProgress(id));
    }

    // ---- KPI实时监控 ----
    @GetMapping("/kpi-monitors")
    public ResponseResult<List<DsteKpiMonitor>> kpiMonitors(@RequestParam(required = false) Long kpiId) {
        return ResponseResult.ok(dsteService.listKpiMonitors(kpiId));
    }
    @PostMapping("/kpi-monitor")
    public ResponseResult<DsteKpiMonitor> saveKpiMonitor(@RequestBody DsteKpiMonitor m) {
        return ResponseResult.ok(dsteService.saveKpiMonitor(m));
    }
    @PutMapping("/kpi-monitor/{id}")
    public ResponseResult<Boolean> updateKpiMonitor(@PathVariable Long id, @RequestBody DsteKpiMonitor m) {
        m.setId(id);
        return ResponseResult.ok(dsteService.updateKpiMonitor(m));
    }
    @DeleteMapping("/kpi-monitor/{id}")
    public ResponseResult<Boolean> deleteKpiMonitor(@PathVariable Long id) {
        return ResponseResult.ok(dsteService.deleteKpiMonitor(id));
    }

    // ---- 风险预警 ----
    @GetMapping("/risks")
    public ResponseResult<List<DsteRiskWarning>> risks() {
        return ResponseResult.ok(dsteService.listRisks());
    }
    @PostMapping("/risk")
    public ResponseResult<DsteRiskWarning> saveRisk(@RequestBody DsteRiskWarning r) {
        return ResponseResult.ok(dsteService.saveRisk(r));
    }
    @PutMapping("/risk/{id}")
    public ResponseResult<Boolean> updateRisk(@PathVariable Long id, @RequestBody DsteRiskWarning r) {
        r.setId(id);
        return ResponseResult.ok(dsteService.updateRisk(r));
    }
    @DeleteMapping("/risk/{id}")
    public ResponseResult<Boolean> deleteRisk(@PathVariable Long id) {
        return ResponseResult.ok(dsteService.deleteRisk(id));
    }

    // ---- 执行督办 ----
    @GetMapping("/supervisions")
    public ResponseResult<List<DsteSupervision>> supervisions() {
        return ResponseResult.ok(dsteService.listSupervisions());
    }
    @PostMapping("/supervision")
    public ResponseResult<DsteSupervision> saveSupervision(@RequestBody DsteSupervision s) {
        return ResponseResult.ok(dsteService.saveSupervision(s));
    }
    @PutMapping("/supervision/{id}")
    public ResponseResult<Boolean> updateSupervision(@PathVariable Long id, @RequestBody DsteSupervision s) {
        s.setId(id);
        return ResponseResult.ok(dsteService.updateSupervision(s));
    }
    @DeleteMapping("/supervision/{id}")
    public ResponseResult<Boolean> deleteSupervision(@PathVariable Long id) {
        return ResponseResult.ok(dsteService.deleteSupervision(id));
    }

    // ---- 阶段性复盘 ----
    @GetMapping("/reviews")
    public ResponseResult<List<DsteReview>> reviews() {
        return ResponseResult.ok(dsteService.listReviews());
    }
    @PostMapping("/review")
    public ResponseResult<DsteReview> saveReview(@RequestBody DsteReview r) {
        return ResponseResult.ok(dsteService.saveReview(r));
    }
    @PutMapping("/review/{id}")
    public ResponseResult<Boolean> updateReview(@PathVariable Long id, @RequestBody DsteReview r) {
        r.setId(id);
        return ResponseResult.ok(dsteService.updateReview(r));
    }
    @DeleteMapping("/review/{id}")
    public ResponseResult<Boolean> deleteReview(@PathVariable Long id) {
        return ResponseResult.ok(dsteService.deleteReview(id));
    }

    // ---- 战略迭代 ----
    @GetMapping("/iterations")
    public ResponseResult<List<DsteStrategicIteration>> iterations() {
        return ResponseResult.ok(dsteService.listIterations());
    }
    @PostMapping("/iteration")
    public ResponseResult<DsteStrategicIteration> saveIteration(@RequestBody DsteStrategicIteration i) {
        return ResponseResult.ok(dsteService.saveIteration(i));
    }
    @PutMapping("/iteration/{id}")
    public ResponseResult<Boolean> updateIteration(@PathVariable Long id, @RequestBody DsteStrategicIteration i) {
        i.setId(id);
        return ResponseResult.ok(dsteService.updateIteration(i));
    }
    @DeleteMapping("/iteration/{id}")
    public ResponseResult<Boolean> deleteIteration(@PathVariable Long id) {
        return ResponseResult.ok(dsteService.deleteIteration(id));
    }

    // ---- 执行台账汇总 ----
    @GetMapping("/exec-summary")
    public ResponseResult<Map<String, Object>> execSummary() {
        return ResponseResult.ok(dsteService.execLedgerSummary());
    }
}
