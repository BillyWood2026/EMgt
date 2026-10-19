package com.example.partyaffairsmgt.stats.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.stats.StatsService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 党建驾驶舱（需求 11.1）：核心指标卡/趋势/排行/组织分布，三屏联动同一数据源。
 */
@RestController
@RequestMapping("/api/party/stats/dashboard")
@CrossOrigin(origins = "*")
public class DashboardStatController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/overview")
    public ResponseResult<Map<String, Object>> overview() {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(statsService.overview());
    }

    @GetMapping("/trend")
    public ResponseResult<List<Map<String, Object>>> trend() {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(statsService.trend());
    }

    @GetMapping("/rank")
    public ResponseResult<Map<String, Object>> rank() {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(statsService.rank());
    }

    @GetMapping("/org-distribution")
    public ResponseResult<List<Map<String, Object>>> orgDistribution() {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(statsService.orgDistribution());
    }
}
