package com.example.flowengine.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.flowengine.common.ResponseResult;
import com.example.flowengine.entity.FlowTimeoutLog;
import com.example.flowengine.service.FlowTimeoutLogService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/flow/timeout")
public class FlowTimeoutLogController {

    private final FlowTimeoutLogService flowTimeoutLogService;

    public FlowTimeoutLogController(FlowTimeoutLogService flowTimeoutLogService) {
        this.flowTimeoutLogService = flowTimeoutLogService;
    }

    @GetMapping("/list")
    public ResponseResult<List<FlowTimeoutLog>> list(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<FlowTimeoutLog> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.like(FlowTimeoutLog::getAssignee, keyword);
        }
        w.orderByDesc(FlowTimeoutLog::getId);
        return ResponseResult.ok(flowTimeoutLogService.list(w));
    }

    @GetMapping("/{id}")
    public ResponseResult<FlowTimeoutLog> getById(@PathVariable Long id) {
        return ResponseResult.ok(flowTimeoutLogService.getById(id));
    }

    @PostMapping
    public ResponseResult<FlowTimeoutLog> create(@RequestBody FlowTimeoutLog entity) {
        entity.setId(null);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        flowTimeoutLogService.save(entity);
        return ResponseResult.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<FlowTimeoutLog> update(@PathVariable Long id, @RequestBody FlowTimeoutLog entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        flowTimeoutLogService.updateById(entity);
        return ResponseResult.ok(flowTimeoutLogService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(flowTimeoutLogService.removeById(id));
    }
}
