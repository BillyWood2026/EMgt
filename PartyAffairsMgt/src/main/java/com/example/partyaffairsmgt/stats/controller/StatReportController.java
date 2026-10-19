package com.example.partyaffairsmgt.stats.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.stats.StatsService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 固定报表中心（需求 11.2）：7张预置报表，统一 columns+rows 结构。
 */
@RestController
@RequestMapping("/api/party/stats/report")
@CrossOrigin(origins = "*")
public class StatReportController {

    @Autowired
    private StatsService statsService;

    /** 报表清单（含名称，供前端渲染页签）。 */
    @GetMapping("/catalog")
    public ResponseResult<List<Map<String, Object>>> catalog() {
        PartyRole.requireAny(PartyRole.READ);
        List<Map<String, Object>> list = new java.util.ArrayList<>();
        StatsService.REPORT_NAMES.forEach((k, v) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("key", k);
            m.put("name", v);
            list.add(m);
        });
        return ResponseResult.success(list);
    }

    /** 报表数据。key: member/dues/activity/development/news/opinion/assessment */
    @GetMapping("/{key}")
    public ResponseResult<Map<String, Object>> report(@PathVariable String key) {
        PartyRole.requireAny(PartyRole.READ);
        List<Map<String, Object>> rows = statsService.report(key);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("key", key);
        r.put("name", StatsService.REPORT_NAMES.get(key));
        r.put("rows", rows);
        r.put("total", rows.size());
        return ResponseResult.success(r);
    }
}
