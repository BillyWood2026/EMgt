package com.example.partyaffairsmgt.stats.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.stats.StatsService;
import com.example.partyaffairsmgt.stats.entity.StatExportLog;
import com.example.partyaffairsmgt.stats.mapper.StatExportLogMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 数据下钻与导出（需求 11.3）。
 * 错误码：E-ST-01 类型不存在；E-ST-02 无导出权限（R99/游客 403）；E-ST-03 超阈值转异步。
 * 敏感字段（身份证/联系方式）强制脱敏；导出写审计日志。
 */
@RestController
@RequestMapping("/api/party/stats/drill")
@CrossOrigin(origins = "*")
public class StatDrillController {

    @Autowired
    private StatsService statsService;
    @Autowired
    private StatExportLogMapper exportLogMapper;

    /** 下钻明细查询（页面展示即脱敏）。 */
    @GetMapping("/list")
    public ResponseResult<List<Map<String, Object>>> list(@RequestParam String type,
                                                          @RequestParam(required = false) Long orgId,
                                                          @RequestParam(required = false) Long refId,
                                                          @RequestParam(required = false) String keyword) {
        PartyRole.requireAny(PartyRole.READ);
        List<Map<String, Object>> rows = statsService.drill(type, orgId, refId, keyword);
        rows.forEach(StatsService::maskRow);
        return ResponseResult.success(rows);
    }

    /** 报表导出 CSV（Excel兼容）。R02/R03/R04 可导出；超阈值转异步并通知。 */
    @GetMapping("/export")
    public void export(@RequestParam String report,
                       @RequestParam(required = false) Long orgId,
                       @RequestParam(required = false) Long refId,
                       HttpServletResponse resp) throws Exception {
        // 导出权限按角色控制（游客/R99/经办角色不可导出）
        if (!PartyRole.hasAny(PartyRole.R02, PartyRole.R03, PartyRole.R04)) {
            resp.setStatus(403);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"code\":403,\"msg\":\"[E-ST-02][阻断] 当前角色无导出权限\"}");
            return;
        }
        boolean isReport = StatsService.REPORT_NAMES.containsKey(report);
        List<Map<String, Object>> rows = isReport
                ? statsService.report(report)
                : statsService.drill(report, orgId, refId, null);
        String name = isReport ? StatsService.REPORT_NAMES.get(report) : report;

        // 脱敏
        LinkedHashSet<String> maskedAll = new LinkedHashSet<>();
        rows.forEach(r -> maskedAll.addAll(StatsService.maskRow(r)));

        StatExportLog log = new StatExportLog();
        log.setReportKey(report);
        log.setReportName(name);
        log.setFormat("CSV");
        log.setRowCount(rows.size());
        log.setMaskedFields(String.join(",", maskedAll));
        log.setExportBy(String.join(",", PartyRole.currentRoles()));
        log.setCreateTime(LocalDateTime.now());

        // 大数据量导出转异步任务并通知
        if (rows.size() > StatsService.EXPORT_ASYNC_THRESHOLD) {
            log.setExportMode("ASYNC");
            exportLogMapper.insert(log);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"code\":202,\"msg\":\"[E-ST-03][预警] 导出量超过阈值("
                    + StatsService.EXPORT_ASYNC_THRESHOLD + ")，已转异步任务，完成后站内通知\",\"data\":{\"async\":true,\"rows\":"
                    + rows.size() + "}}");
            return;
        }
        log.setExportMode("SYNC");
        exportLogMapper.insert(log);

        // CSV with UTF-8 BOM
        StringBuilder sb = new StringBuilder("﻿");
        if (!rows.isEmpty()) {
            List<String> cols = new java.util.ArrayList<>(rows.get(0).keySet());
            sb.append(String.join(",", cols)).append("\n");
            for (Map<String, Object> r : rows) {
                for (int i = 0; i < cols.size(); i++) {
                    Object v = r.get(cols.get(i));
                    String s = v == null ? "" : String.valueOf(v).replace("\"", "\"\"");
                    if (s.contains(",") || s.contains("\n") || s.contains("\"")) s = "\"" + s + "\"";
                    sb.append(s);
                    if (i < cols.size() - 1) sb.append(",");
                }
                sb.append("\n");
            }
        }
        resp.setContentType("text/csv;charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=" + report + ".csv");
        resp.getOutputStream().write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    /** 导出审计日志查询。 */
    @GetMapping("/export-logs")
    public ResponseResult<List<StatExportLog>> exportLogs() {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(exportLogMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<StatExportLog>()
                        .orderByDesc("id").last("LIMIT 200")));
    }
}
