package com.example.hrplanning.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.example.hrplanning.entity.DemandForecast;
import com.example.hrplanning.service.DemandForecastService;
import com.example.hrplanning.util.PageResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 需求预测 /api/hr-planning/forecast
 * 前端 hr-planning/Index.vue「需求预测」tab：GET /page 直接读取 {records,total}
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/hr-planning/forecast")
public class DemandForecastController {

    @Autowired
    private DemandForecastService demandForecastService;

    @GetMapping("/page")
    public Map<String, Object> page(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "orgCode", required = false) String orgCode,
            @RequestParam(value = "planNo", required = false) String planNo) {
        IPage<DemandForecast> page = demandForecastService.page(pageNum, pageSize, orgCode, planNo);
        return PageResponses.of(page);
    }

    @GetMapping("/list")
    public ResponseResult<java.util.List<DemandForecast>> list() {
        return ResponseResult.success(demandForecastService.list());
    }

    @GetMapping("/{id}")
    public ResponseResult<DemandForecast> getById(@PathVariable Long id) {
        return ResponseResult.success(demandForecastService.getById(id));
    }

    @PostMapping
    public ResponseResult<Void> create(@RequestBody DemandForecast forecast) {
        demandForecastService.save(forecast);
        return ResponseResult.success();
    }

    @PutMapping("/{id}")
    public ResponseResult<Void> update(@PathVariable Long id, @RequestBody DemandForecast forecast) {
        forecast.setId(id);
        demandForecastService.updateById(forecast);
        return ResponseResult.success();
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        demandForecastService.removeById(id);
        return ResponseResult.success();
    }
}
