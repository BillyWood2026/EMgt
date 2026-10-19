package com.example.strategymgt.blm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.strategymgt.blm.entity.*;
import com.example.strategymgt.blm.service.BlmService;
import com.example.strategymgt.common.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/strategy/blm")
public class BlmController {

    private final BlmService blmService;

    public BlmController(BlmService blmService) {
        this.blmService = blmService;
    }

    // ---- 差距分析台账 ----
    @GetMapping("/gaps")
    public ResponseResult<List<BlmGapAnalysis>> gaps(@RequestParam(required = false) Integer planYear) {
        return ResponseResult.ok(blmService.listGaps(planYear));
    }

    @PostMapping("/gap")
    public ResponseResult<BlmGapAnalysis> saveGap(@RequestBody BlmGapAnalysis gap) {
        return ResponseResult.ok(blmService.saveGap(gap));
    }

    @PutMapping("/gap/{id}")
    public ResponseResult<Boolean> updateGap(@PathVariable Long id, @RequestBody BlmGapAnalysis gap) {
        gap.setId(id);
        return ResponseResult.ok(blmService.updateGap(gap));
    }

    @DeleteMapping("/gap/{id}")
    public ResponseResult<Boolean> deleteGap(@PathVariable Long id) {
        return ResponseResult.ok(blmService.deleteGap(id));
    }

    // ---- 市场洞察分析 ----
    @GetMapping("/insights")
    public ResponseResult<List<BlmMarketInsight>> insights(@RequestParam(required = false) Integer planYear) {
        return ResponseResult.ok(blmService.listInsights(planYear));
    }

    @PostMapping("/insight")
    public ResponseResult<BlmMarketInsight> saveInsight(@RequestBody BlmMarketInsight insight) {
        return ResponseResult.ok(blmService.saveInsight(insight));
    }

    @PutMapping("/insight/{id}")
    public ResponseResult<Boolean> updateInsight(@PathVariable Long id, @RequestBody BlmMarketInsight insight) {
        insight.setId(id);
        return ResponseResult.ok(blmService.updateInsight(insight));
    }

    @DeleteMapping("/insight/{id}")
    public ResponseResult<Boolean> deleteInsight(@PathVariable Long id) {
        return ResponseResult.ok(blmService.deleteInsight(id));
    }

    // ---- 战略意图定义 ----
    @GetMapping("/intents")
    public ResponseResult<List<BlmStrategicIntent>> intents() {
        return ResponseResult.ok(blmService.listIntents());
    }

    @PostMapping("/intent")
    public ResponseResult<BlmStrategicIntent> saveIntent(@RequestBody BlmStrategicIntent intent) {
        return ResponseResult.ok(blmService.saveIntent(intent));
    }

    @PutMapping("/intent/{id}")
    public ResponseResult<Boolean> updateIntent(@PathVariable Long id, @RequestBody BlmStrategicIntent intent) {
        intent.setId(id);
        return ResponseResult.ok(blmService.updateIntent(intent));
    }

    @DeleteMapping("/intent/{id}")
    public ResponseResult<Boolean> deleteIntent(@PathVariable Long id) {
        return ResponseResult.ok(blmService.deleteIntent(id));
    }

    // ---- 创新焦点管理 ----
    @GetMapping("/innovations")
    public ResponseResult<List<BlmInnovationFocus>> innovations() {
        return ResponseResult.ok(blmService.listInnovations());
    }

    @PostMapping("/innovation")
    public ResponseResult<BlmInnovationFocus> saveInnovation(@RequestBody BlmInnovationFocus inn) {
        return ResponseResult.ok(blmService.saveInnovation(inn));
    }

    @PutMapping("/innovation/{id}")
    public ResponseResult<Boolean> updateInnovation(@PathVariable Long id, @RequestBody BlmInnovationFocus inn) {
        inn.setId(id);
        return ResponseResult.ok(blmService.updateInnovation(inn));
    }

    @DeleteMapping("/innovation/{id}")
    public ResponseResult<Boolean> deleteInnovation(@PathVariable Long id) {
        return ResponseResult.ok(blmService.deleteInnovation(id));
    }

    // ---- 业务设计配置 ----
    @GetMapping("/designs")
    public ResponseResult<List<BlmBusinessDesign>> designs() {
        return ResponseResult.ok(blmService.listDesigns());
    }

    @PostMapping("/design")
    public ResponseResult<BlmBusinessDesign> saveDesign(@RequestBody BlmBusinessDesign design) {
        return ResponseResult.ok(blmService.saveDesign(design));
    }

    @PutMapping("/design/{id}")
    public ResponseResult<Boolean> updateDesign(@PathVariable Long id, @RequestBody BlmBusinessDesign design) {
        design.setId(id);
        return ResponseResult.ok(blmService.updateDesign(design));
    }

    @DeleteMapping("/design/{id}")
    public ResponseResult<Boolean> deleteDesign(@PathVariable Long id) {
        return ResponseResult.ok(blmService.deleteDesign(id));
    }

    // ---- SP中长期战略规划台账 ----
    @GetMapping("/sp-plans")
    public ResponseResult<List<BlmSpPlan>> spPlans() {
        return ResponseResult.ok(blmService.listSpPlans());
    }

    @PostMapping("/sp-plan")
    public ResponseResult<BlmSpPlan> saveSpPlan(@RequestBody BlmSpPlan plan) {
        return ResponseResult.ok(blmService.saveSpPlan(plan));
    }

    @PutMapping("/sp-plan/{id}")
    public ResponseResult<Boolean> updateSpPlan(@PathVariable Long id, @RequestBody BlmSpPlan plan) {
        plan.setId(id);
        return ResponseResult.ok(blmService.updateSpPlan(plan));
    }

    @DeleteMapping("/sp-plan/{id}")
    public ResponseResult<Boolean> deleteSpPlan(@PathVariable Long id) {
        return ResponseResult.ok(blmService.deleteSpPlan(id));
    }

    @PostMapping("/sp-plan/{id}/publish")
    public ResponseResult<BlmSpPlan> publishSp(@PathVariable Long id) {
        return ResponseResult.ok(blmService.publishSp(id));
    }

    @GetMapping("/sp-plan/{id}/alignment")
    public ResponseResult<Map<String, Object>> spAlignment(@PathVariable Long id) {
        Map<String, Object> r = new HashMap<>();
        r.put("spId", id);
        r.put("alignmentScore", blmService.checkSpAlignment(id));
        return ResponseResult.ok(r);
    }

    // ---- 战略投资组合管理 ----
    @GetMapping("/portfolios")
    public ResponseResult<List<BlmInvestmentPortfolio>> portfolios(@RequestParam(required = false) Long spId) {
        return ResponseResult.ok(blmService.listPortfolios(spId));
    }

    @PostMapping("/portfolio")
    public ResponseResult<BlmInvestmentPortfolio> savePortfolio(@RequestBody BlmInvestmentPortfolio p) {
        return ResponseResult.ok(blmService.savePortfolio(p));
    }

    @PutMapping("/portfolio/{id}")
    public ResponseResult<Boolean> updatePortfolio(@PathVariable Long id, @RequestBody BlmInvestmentPortfolio p) {
        p.setId(id);
        return ResponseResult.ok(blmService.updatePortfolio(p));
    }

    @DeleteMapping("/portfolio/{id}")
    public ResponseResult<Boolean> deletePortfolio(@PathVariable Long id) {
        return ResponseResult.ok(blmService.deletePortfolio(id));
    }

    @GetMapping("/portfolios/ranked")
    public ResponseResult<List<BlmInvestmentPortfolio>> rankedPortfolios() {
        return ResponseResult.ok(blmService.rankPortfolios());
    }
}
