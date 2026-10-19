package com.example.flowengine.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.flowengine.common.ResponseResult;
import com.example.flowengine.entity.FlowCallbackLog;
import com.example.flowengine.service.FlowCallbackLogService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/flow/callback")
public class FlowCallbackLogController {

    private final FlowCallbackLogService flowCallbackLogService;

    public FlowCallbackLogController(FlowCallbackLogService flowCallbackLogService) {
        this.flowCallbackLogService = flowCallbackLogService;
    }

    @GetMapping("/list")
    public ResponseResult<List<FlowCallbackLog>> list(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<FlowCallbackLog> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.and(k -> k.like(FlowCallbackLog::getBizCode, keyword).or().like(FlowCallbackLog::getEvent, keyword));
        }
        w.orderByDesc(FlowCallbackLog::getId);
        return ResponseResult.ok(flowCallbackLogService.list(w));
    }

    @GetMapping("/{id}")
    public ResponseResult<FlowCallbackLog> getById(@PathVariable Long id) {
        return ResponseResult.ok(flowCallbackLogService.getById(id));
    }

    @PostMapping
    public ResponseResult<FlowCallbackLog> create(@RequestBody FlowCallbackLog entity) {
        entity.setId(null);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        flowCallbackLogService.save(entity);
        return ResponseResult.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<FlowCallbackLog> update(@PathVariable Long id, @RequestBody FlowCallbackLog entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        flowCallbackLogService.updateById(entity);
        return ResponseResult.ok(flowCallbackLogService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(flowCallbackLogService.removeById(id));
    }
}
