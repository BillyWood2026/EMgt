package com.example.partyaffairsmgt.org.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.org.entity.HeartTalk;
import com.example.partyaffairsmgt.org.entity.TalkCoverage;
import com.example.partyaffairsmgt.org.entity.TalkPlan;
import com.example.partyaffairsmgt.org.service.HeartTalkService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 4.5 谈心谈话接口。 */
@RestController
@RequestMapping("/api/party/org/heart-talk")
@CrossOrigin(origins = "*")
public class HeartTalkController {

    @Autowired private HeartTalkService service;

    @GetMapping("/list")
    public ResponseResult<List<HeartTalk>> list(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String followStatus,
            @RequestParam(required = false) String keyword) {
        return ResponseResult.success(service.list(orgId, year, followStatus, keyword));
    }

    @PostMapping
    public ResponseResult<HeartTalk> create(@RequestBody HeartTalk t) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07);
        return ResponseResult.success(service.save(t));
    }

    @PutMapping
    public ResponseResult<HeartTalk> update(@RequestBody HeartTalk t) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07);
        return ResponseResult.success(service.save(t));
    }

    @GetMapping("/unresolved")
    public ResponseResult<List<HeartTalk>> unresolved() {
        return ResponseResult.success(service.unresolvedIssues());
    }

    // ---------- 计划与覆盖 ----------

    @GetMapping("/plan/list")
    public ResponseResult<List<TalkPlan>> plans(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer year) {
        return ResponseResult.success(service.plans(orgId, year));
    }

    @PostMapping("/plan")
    public ResponseResult<TalkPlan> savePlan(@RequestBody TalkPlan plan) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07);
        return ResponseResult.success(service.savePlan(plan));
    }

    @PutMapping("/plan")
    public ResponseResult<TalkPlan> updatePlan(@RequestBody TalkPlan plan) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07);
        return ResponseResult.success(service.savePlan(plan));
    }

    @PostMapping("/plan/{id}/generate")
    public ResponseResult<List<TalkCoverage>> generate(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07);
        return ResponseResult.success(service.generateCoverage(id));
    }

    @GetMapping("/plan/{id}/coverage")
    public ResponseResult<List<TalkCoverage>> coverage(@PathVariable Long id) {
        return ResponseResult.success(service.coverage(id));
    }

    @GetMapping("/plan/{id}/stats")
    public ResponseResult<Map<String, Object>> stats(@PathVariable Long id) {
        return ResponseResult.success(service.coverageStats(id));
    }
}
