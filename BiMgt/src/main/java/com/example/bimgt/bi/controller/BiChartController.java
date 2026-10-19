package com.example.bimgt.bi.controller;

import com.example.bimgt.bi.entity.BiChart;
import com.example.bimgt.bi.service.BiChartService;
import com.example.bimgt.common.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bi/chart")
public class BiChartController {

    private final BiChartService service;

    public BiChartController(BiChartService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public ResponseResult<List<BiChart>> list() {
        return ResponseResult.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public ResponseResult<BiChart> get(@PathVariable Long id) {
        return ResponseResult.ok(service.getById(id));
    }

    @PostMapping
    public ResponseResult<BiChart> save(@RequestBody BiChart c) {
        return ResponseResult.ok(service.upsert(c));
    }

    @PutMapping("/{id}")
    public ResponseResult<BiChart> update(@PathVariable Long id, @RequestBody BiChart c) {
        c.setId(id);
        return ResponseResult.ok(service.upsert(c));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(service.removeById(id));
    }
}
