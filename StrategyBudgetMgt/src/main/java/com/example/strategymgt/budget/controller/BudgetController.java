package com.example.strategymgt.budget.controller;

import com.example.strategymgt.budget.entity.*;
import com.example.strategymgt.budget.service.BudgetService;
import com.example.strategymgt.common.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/strategy/budget")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    // ---- 主数据 ----
    @GetMapping("/subjects")
    public ResponseResult<List<BudgetSubject>> subjects() { return ResponseResult.ok(budgetService.listSubjects()); }
    @PostMapping("/subject")
    public ResponseResult<BudgetSubject> saveSubject(@RequestBody BudgetSubject s) { return ResponseResult.ok(budgetService.saveSubject(s)); }
    @PutMapping("/subject/{id}")
    public ResponseResult<Boolean> updateSubject(@PathVariable Long id, @RequestBody BudgetSubject s) {
        s.setId(id);
        return ResponseResult.ok(budgetService.updateSubject(s));
    }
    @DeleteMapping("/subject/{id}")
    public ResponseResult<Boolean> deleteSubject(@PathVariable Long id) { return ResponseResult.ok(budgetService.deleteSubject(id)); }

    @GetMapping("/dimensions")
    public ResponseResult<List<BudgetDimension>> dimensions() { return ResponseResult.ok(budgetService.listDimensions()); }
    @PostMapping("/dimension")
    public ResponseResult<BudgetDimension> saveDimension(@RequestBody BudgetDimension d) { return ResponseResult.ok(budgetService.saveDimension(d)); }
    @PutMapping("/dimension/{id}")
    public ResponseResult<Boolean> updateDimension(@PathVariable Long id, @RequestBody BudgetDimension d) {
        d.setId(id);
        return ResponseResult.ok(budgetService.updateDimension(d));
    }
    @DeleteMapping("/dimension/{id}")
    public ResponseResult<Boolean> deleteDimension(@PathVariable Long id) { return ResponseResult.ok(budgetService.deleteDimension(id)); }

    @GetMapping("/params")
    public ResponseResult<List<BudgetParam>> params() { return ResponseResult.ok(budgetService.listParams()); }
    @PostMapping("/param")
    public ResponseResult<BudgetParam> saveParam(@RequestBody BudgetParam p) { return ResponseResult.ok(budgetService.saveParam(p)); }
    @PutMapping("/param/{id}")
    public ResponseResult<Boolean> updateParam(@PathVariable Long id, @RequestBody BudgetParam p) {
        p.setId(id);
        return ResponseResult.ok(budgetService.updateParam(p));
    }
    @DeleteMapping("/param/{id}")
    public ResponseResult<Boolean> deleteParam(@PathVariable Long id) { return ResponseResult.ok(budgetService.deleteParam(id)); }

    @GetMapping("/rules")
    public ResponseResult<List<BudgetControlRule>> rules() { return ResponseResult.ok(budgetService.listRules()); }
    @PostMapping("/rule")
    public ResponseResult<BudgetControlRule> saveRule(@RequestBody BudgetControlRule r) { return ResponseResult.ok(budgetService.saveRule(r)); }
    @PutMapping("/rule/{id}")
    public ResponseResult<Boolean> updateRule(@PathVariable Long id, @RequestBody BudgetControlRule r) {
        r.setId(id);
        return ResponseResult.ok(budgetService.updateRule(r));
    }
    @DeleteMapping("/rule/{id}")
    public ResponseResult<Boolean> deleteRule(@PathVariable Long id) { return ResponseResult.ok(budgetService.deleteRule(id)); }

    // ---- 三上三下 ----
    @GetMapping("/versions")
    public ResponseResult<List<BudgetVersion>> versions(@RequestParam(required = false) Integer year) {
        return ResponseResult.ok(budgetService.listVersions(year));
    }
    @PostMapping("/version")
    public ResponseResult<BudgetVersion> saveVersion(@RequestBody BudgetVersion v) { return ResponseResult.ok(budgetService.saveVersion(v)); }
    @PutMapping("/version/{id}")
    public ResponseResult<Boolean> updateVersion(@PathVariable Long id, @RequestBody BudgetVersion v) {
        v.setId(id);
        return ResponseResult.ok(budgetService.updateVersion(v));
    }
    @DeleteMapping("/version/{id}")
    public ResponseResult<Boolean> deleteVersion(@PathVariable Long id) { return ResponseResult.ok(budgetService.deleteVersion(id)); }

    @GetMapping("/annual")
    public ResponseResult<List<AnnualBudget>> annual(@RequestParam(required = false) Integer year) {
        return ResponseResult.ok(budgetService.listAnnualBudgets(year));
    }
    @PostMapping("/annual")
    public ResponseResult<AnnualBudget> saveAnnual(@RequestBody AnnualBudget b) { return ResponseResult.ok(budgetService.saveAnnualBudget(b)); }
    @PutMapping("/annual/{id}")
    public ResponseResult<Boolean> updateAnnual(@PathVariable Long id, @RequestBody AnnualBudget b) {
        b.setId(id);
        return ResponseResult.ok(budgetService.updateAnnualBudget(b));
    }
    @DeleteMapping("/annual/{id}")
    public ResponseResult<Boolean> deleteAnnual(@PathVariable Long id) { return ResponseResult.ok(budgetService.deleteAnnualBudget(id)); }

    // ---- 项目四算 ----
    @GetMapping("/projects")
    public ResponseResult<List<ProjectBudget>> projects() { return ResponseResult.ok(budgetService.listProjects()); }
    @PostMapping("/project")
    public ResponseResult<ProjectBudget> saveProject(@RequestBody ProjectBudget p) { return ResponseResult.ok(budgetService.saveProject(p)); }
    @PutMapping("/project/{id}")
    public ResponseResult<Boolean> updateProject(@PathVariable Long id, @RequestBody ProjectBudget p) {
        p.setId(id);
        return ResponseResult.ok(budgetService.updateProject(p));
    }
    @DeleteMapping("/project/{id}")
    public ResponseResult<Boolean> deleteProject(@PathVariable Long id) { return ResponseResult.ok(budgetService.deleteProject(id)); }

    // ---- 实时控制引擎 ----
    @GetMapping("/occupations")
    public ResponseResult<List<BudgetOccupation>> occupations(@RequestParam(required = false) Long budgetId) {
        return ResponseResult.ok(budgetService.listOccupations(budgetId));
    }
    @PostMapping("/occupy")
    public ResponseResult<BudgetOccupation> occupy(@RequestBody BudgetOccupation o) { return ResponseResult.ok(budgetService.occupy(o)); }
    @PutMapping("/occupy/{id}")
    public ResponseResult<Boolean> updateOccupation(@PathVariable Long id, @RequestBody BudgetOccupation o) {
        o.setId(id);
        return ResponseResult.ok(budgetService.updateOccupation(o));
    }
    @DeleteMapping("/occupy/{id}")
    public ResponseResult<Boolean> deleteOccupation(@PathVariable Long id) { return ResponseResult.ok(budgetService.deleteOccupation(id)); }
    @GetMapping("/exec-rate/{id}")
    public ResponseResult<BigDecimal> execRate(@PathVariable Long id) { return ResponseResult.ok(budgetService.execRate(id)); }

    // ---- 调整追加调剂 ----
    @GetMapping("/changes")
    public ResponseResult<List<BudgetChange>> changes() { return ResponseResult.ok(budgetService.listChanges()); }
    @PostMapping("/change")
    public ResponseResult<BudgetChange> saveChange(@RequestBody BudgetChange c) { return ResponseResult.ok(budgetService.saveChange(c)); }
    @PutMapping("/change/{id}")
    public ResponseResult<Boolean> updateChange(@PathVariable Long id, @RequestBody BudgetChange c) {
        c.setId(id);
        return ResponseResult.ok(budgetService.updateChange(c));
    }
    @DeleteMapping("/change/{id}")
    public ResponseResult<Boolean> deleteChange(@PathVariable Long id) { return ResponseResult.ok(budgetService.deleteChange(id)); }

    // ---- 滚动预测 ----
    @GetMapping("/forecasts")
    public ResponseResult<List<RollingForecast>> forecasts() { return ResponseResult.ok(budgetService.listForecasts()); }
    @PostMapping("/forecast")
    public ResponseResult<RollingForecast> saveForecast(@RequestBody RollingForecast f) { return ResponseResult.ok(budgetService.saveForecast(f)); }
    @PutMapping("/forecast/{id}")
    public ResponseResult<Boolean> updateForecast(@PathVariable Long id, @RequestBody RollingForecast f) {
        f.setId(id);
        return ResponseResult.ok(budgetService.updateForecast(f));
    }
    @DeleteMapping("/forecast/{id}")
    public ResponseResult<Boolean> deleteForecast(@PathVariable Long id) { return ResponseResult.ok(budgetService.deleteForecast(id)); }

    // ---- 差异分析 ----
    @GetMapping("/variances")
    public ResponseResult<List<BudgetVariance>> variances() { return ResponseResult.ok(budgetService.listVariances()); }
    @PostMapping("/variance")
    public ResponseResult<BudgetVariance> saveVariance(@RequestBody BudgetVariance v) { return ResponseResult.ok(budgetService.saveVariance(v)); }
    @PutMapping("/variance/{id}")
    public ResponseResult<Boolean> updateVariance(@PathVariable Long id, @RequestBody BudgetVariance v) {
        v.setId(id);
        return ResponseResult.ok(budgetService.updateVariance(v));
    }
    @DeleteMapping("/variance/{id}")
    public ResponseResult<Boolean> deleteVariance(@PathVariable Long id) { return ResponseResult.ok(budgetService.deleteVariance(id)); }

    // ---- 内控审计 ----
    @GetMapping("/audits")
    public ResponseResult<List<BudgetAudit>> audits() { return ResponseResult.ok(budgetService.listAudits()); }
    @PostMapping("/audit")
    public ResponseResult<BudgetAudit> saveAudit(@RequestBody BudgetAudit a) { return ResponseResult.ok(budgetService.saveAudit(a)); }
    @PutMapping("/audit/{id}")
    public ResponseResult<Boolean> updateAudit(@PathVariable Long id, @RequestBody BudgetAudit a) {
        a.setId(id);
        return ResponseResult.ok(budgetService.updateAudit(a));
    }
    @DeleteMapping("/audit/{id}")
    public ResponseResult<Boolean> deleteAudit(@PathVariable Long id) { return ResponseResult.ok(budgetService.deleteAudit(id)); }

    // ---- 总览 ----
    @GetMapping("/overview")
    public ResponseResult<Map<String, Object>> overview(@RequestParam(required = false) Integer year) {
        return ResponseResult.ok(budgetService.overview(year));
    }
}
