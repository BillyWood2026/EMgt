package com.example.discipline.supervision.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.discipline.common.ResponseResult;
import com.example.discipline.supervision.entity.SupervisionRecord;
import com.example.discipline.supervision.service.SupervisionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/discipline/supervision")
@CrossOrigin(origins = "*")
public class SupervisionRecordController {

    @Autowired
    private SupervisionRecordService recordService;

    @GetMapping("/list")
    public ResponseResult<Page<SupervisionRecord>> list(@RequestParam(required = false) String recordNo,
                                                        @RequestParam(required = false) String supervisionType,
                                                        @RequestParam(required = false) String riskLevel,
                                                        @RequestParam(required = false) String status,
                                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResponseResult.success(recordService.page(recordNo, supervisionType, riskLevel, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseResult<SupervisionRecord> getById(@PathVariable Long id) {
        return ResponseResult.success(recordService.getById(id));
    }

    @PostMapping
    public ResponseResult<SupervisionRecord> create(@RequestBody SupervisionRecord r) {
        return ResponseResult.success(recordService.create(r));
    }

    @PutMapping("/{id}")
    public ResponseResult<SupervisionRecord> update(@PathVariable Long id, @RequestBody SupervisionRecord r) {
        return ResponseResult.success(recordService.update(id, r));
    }

    /** 发现问题、启动处置：待处置 -> 处置中 */
    @PutMapping("/{id}/handle")
    public ResponseResult<SupervisionRecord> handle(@PathVariable Long id, @RequestBody(required = false) SupervisionRecord body) {
        return ResponseResult.success(recordService.handle(id, body));
    }

    /** 处置办结：处置中 -> 已完成（须填写处置结果） */
    @PutMapping("/{id}/complete")
    public ResponseResult<SupervisionRecord> complete(@PathVariable Long id, @RequestBody(required = false) SupervisionRecord body) {
        return ResponseResult.success(recordService.complete(id, body));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(recordService.delete(id));
    }
}
