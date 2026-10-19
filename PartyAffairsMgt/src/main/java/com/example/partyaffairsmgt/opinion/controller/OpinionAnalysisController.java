package com.example.partyaffairsmgt.opinion.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.opinion.entity.OpinionAnalysis;
import com.example.partyaffairsmgt.opinion.service.OpinionAnalysisService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 舆情分析控制器（需求 8.2：情感/热度/关联度/聚类/批量标注/人工改判）。 */
@RestController
@RequestMapping("/api/party/opinion/analysis")
@CrossOrigin(origins = "*")
public class OpinionAnalysisController {

    @Autowired
    private OpinionAnalysisService service;

    @GetMapping("/list")
    public ResponseResult<List<OpinionAnalysis>> list(@RequestParam(required = false) String sentiment,
                                                      @RequestParam(required = false) String heatLevel,
                                                      @RequestParam(required = false) String eventCluster,
                                                      @RequestParam(required = false) String keyword) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.list(sentiment, heatLevel, eventCluster, keyword));
    }

    @GetMapping("/{id}")
    public ResponseResult<OpinionAnalysis> detail(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.detail(id));
    }

    @PostMapping("/save")
    public ResponseResult<OpinionAnalysis> save(@RequestBody OpinionAnalysis a) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R14);
        return ResponseResult.success(service.save(a));
    }

    /** 对采集信息自动分析 */
    @PostMapping("/analyze/{collectionId}")
    public ResponseResult<OpinionAnalysis> analyze(@PathVariable Long collectionId) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R14);
        return ResponseResult.success(service.analyze(collectionId));
    }

    /** 人工改判情感（理由必填 E-OP-06） */
    @PostMapping("/{id}/rejudge")
    public ResponseResult<OpinionAnalysis> reJudge(@PathVariable Long id,
                                                   @RequestParam String sentiment,
                                                   @RequestParam(required = false) String reason) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R14);
        return ResponseResult.success(service.reJudge(id, sentiment, reason));
    }

    /** 批量标注：属实/不属实/待核实 */
    @PostMapping("/mark")
    public ResponseResult<Integer> batchMark(@RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R14);
        @SuppressWarnings("unchecked")
        List<Number> raw = (List<Number>) body.get("ids");
        List<Long> ids = raw.stream().map(Number::longValue).toList();
        String mark = (String) body.get("markResult");
        return ResponseResult.success(service.batchMark(ids, mark));
    }
}
