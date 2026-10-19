package com.example.bimgt.bi.controller;

import com.example.bimgt.bi.entity.BiMetric;
import com.example.bimgt.bi.service.BiMetricService;
import com.example.bimgt.common.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bi/metric")
public class BiMetricController {

    private final BiMetricService service;

    public BiMetricController(BiMetricService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public ResponseResult<List<BiMetric>> list() {
        return ResponseResult.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public ResponseResult<BiMetric> get(@PathVariable Long id) {
        return ResponseResult.ok(service.getById(id));
    }

    @PostMapping
    public ResponseResult<BiMetric> save(@RequestBody BiMetric m) {
        return ResponseResult.ok(service.upsert(m));
    }

    @PutMapping("/{id}")
    public ResponseResult<BiMetric> update(@PathVariable Long id, @RequestBody BiMetric m) {
        m.setId(id);
        return ResponseResult.ok(service.upsert(m));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(service.removeById(id));
    }
}
