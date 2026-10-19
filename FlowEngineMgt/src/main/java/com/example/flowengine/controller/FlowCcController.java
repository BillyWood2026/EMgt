package com.example.flowengine.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.flowengine.common.ResponseResult;
import com.example.flowengine.entity.FlowCc;
import com.example.flowengine.service.FlowCcService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/flow/cc")
public class FlowCcController {

    private final FlowCcService flowCcService;

    public FlowCcController(FlowCcService flowCcService) {
        this.flowCcService = flowCcService;
    }

    @GetMapping("/list")
    public ResponseResult<List<FlowCc>> list(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<FlowCc> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.like(FlowCc::getCcUser, keyword);
        }
        w.orderByDesc(FlowCc::getId);
        return ResponseResult.ok(flowCcService.list(w));
    }

    @GetMapping("/{id}")
    public ResponseResult<FlowCc> getById(@PathVariable Long id) {
        return ResponseResult.ok(flowCcService.getById(id));
    }

    @PostMapping
    public ResponseResult<FlowCc> create(@RequestBody FlowCc entity) {
        entity.setId(null);
        if (entity.getReadFlag() == null) {
            entity.setReadFlag(0);
        }
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        flowCcService.save(entity);
        return ResponseResult.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<FlowCc> update(@PathVariable Long id, @RequestBody FlowCc entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        flowCcService.updateById(entity);
        return ResponseResult.ok(flowCcService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(flowCcService.removeById(id));
    }
}
