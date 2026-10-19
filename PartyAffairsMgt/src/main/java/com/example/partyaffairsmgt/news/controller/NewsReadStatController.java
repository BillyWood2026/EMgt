package com.example.partyaffairsmgt.news.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.news.service.NewsReadStatService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

/** 阅读统计分析控制器（需求 7.5）。 */
@RestController
@RequestMapping("/api/party/news/read-stat")
@CrossOrigin(origins = "*")
public class NewsReadStatController {

    @Autowired
    private NewsReadStatService readStatService;

    @GetMapping("/list")
    public ResponseResult<List<Map<String, Object>>> list(
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String sourceChannel) {
        return ResponseResult.success(readStatService.ranking(startDate, endDate, null));
    }

    @GetMapping("/ranking")
    public ResponseResult<List<Map<String, Object>>> ranking(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer limit) {
        return ResponseResult.success(readStatService.ranking(startDate, endDate, limit));
    }

    @GetMapping("/hourly")
    public ResponseResult<Map<String, Object>> hourly(
            @RequestParam(required = false) Long articleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseResult.success(readStatService.hourlyDistribution(articleId, date));
    }

    @PostMapping("/record")
    public ResponseResult<Void> recordRead(
            @RequestParam Long articleId,
            @RequestParam(required = false) String sourceChannel,
            @RequestParam(required = false) String deviceId) {
        readStatService.recordRead(articleId, sourceChannel, deviceId);
        return ResponseResult.success(null);
    }
}
