package com.example.flowengine.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.flowengine.common.ResponseResult;
import com.example.flowengine.entity.FlowInstance;
import com.example.flowengine.service.FlowInstanceService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/flow/instance")
public class FlowInstanceController {

    private final FlowInstanceService flowInstanceService;

    public FlowInstanceController(FlowInstanceService flowInstanceService) {
        this.flowInstanceService = flowInstanceService;
    }

    @GetMapping("/list")
    public ResponseResult<List<FlowInstance>> list(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<FlowInstance> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.and(k -> k.like(FlowInstance::getTitle, keyword)
                    .or().like(FlowInstance::getInstNo, keyword)
                    .or().like(FlowInstance::getInitiator, keyword));
        }
        w.orderByDesc(FlowInstance::getId);
        return ResponseResult.ok(flowInstanceService.list(w));
    }

    @GetMapping("/{id}")
    public ResponseResult<FlowInstance> getById(@PathVariable Long id) {
        return ResponseResult.ok(flowInstanceService.getById(id));
    }

    @PostMapping
    public ResponseResult<FlowInstance> create(@RequestBody FlowInstance entity) {
        entity.setId(null);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        flowInstanceService.save(entity);
        return ResponseResult.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<FlowInstance> update(@PathVariable Long id, @RequestBody FlowInstance entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        flowInstanceService.updateById(entity);
        return ResponseResult.ok(flowInstanceService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(flowInstanceService.removeById(id));
    }

    @PostMapping("/start")
    public ResponseResult<FlowInstance> start(@RequestBody FlowInstance req) {
        return ResponseResult.ok(flowInstanceService.start(req));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseResult<Boolean> withdraw(@PathVariable Long id, @RequestParam String operator) {
        flowInstanceService.withdraw(id, operator);
        return ResponseResult.ok(true);
    }
}
