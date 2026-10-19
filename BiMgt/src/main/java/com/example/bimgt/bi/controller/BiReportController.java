package com.example.bimgt.bi.controller;

import com.example.bimgt.bi.entity.BiReport;
import com.example.bimgt.bi.service.BiReportService;
import com.example.bimgt.common.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bi/report")
public class BiReportController {

    private final BiReportService service;

    public BiReportController(BiReportService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public ResponseResult<List<BiReport>> list() {
        return ResponseResult.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public ResponseResult<BiReport> get(@PathVariable Long id) {
        return ResponseResult.ok(service.getById(id));
    }

    @PostMapping
    public ResponseResult<BiReport> save(@RequestBody BiReport r) {
        return ResponseResult.ok(service.upsert(r));
    }

    @PutMapping("/{id}")
    public ResponseResult<BiReport> update(@PathVariable Long id, @RequestBody BiReport r) {
        r.setId(id);
        return ResponseResult.ok(service.upsert(r));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(service.removeById(id));
    }

    /** 发布报表 */
    @PostMapping("/{id}/publish")
    public ResponseResult<BiReport> publish(@PathVariable Long id) {
        return ResponseResult.ok(service.publish(id));
    }

    /** 报表预览: 报表定义+模拟数据 */
    @GetMapping("/{id}/preview")
    public ResponseResult<Map<String, Object>> preview(@PathVariable Long id) {
        return ResponseResult.ok(service.preview(id));
    }
}
