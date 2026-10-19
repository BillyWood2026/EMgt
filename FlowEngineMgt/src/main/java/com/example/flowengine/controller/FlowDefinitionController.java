package com.example.flowengine.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.flowengine.common.ResponseResult;
import com.example.flowengine.entity.FlowDefinition;
import com.example.flowengine.service.FlowDefinitionService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/flow/definition")
public class FlowDefinitionController {

    private final FlowDefinitionService flowDefinitionService;

    public FlowDefinitionController(FlowDefinitionService flowDefinitionService) {
        this.flowDefinitionService = flowDefinitionService;
    }

    @GetMapping("/list")
    public ResponseResult<List<FlowDefinition>> list(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<FlowDefinition> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.and(k -> k.like(FlowDefinition::getDefName, keyword).or().like(FlowDefinition::getDefCode, keyword));
        }
        w.orderByDesc(FlowDefinition::getId);
        return ResponseResult.ok(flowDefinitionService.list(w));
    }

    @GetMapping("/{id}")
    public ResponseResult<FlowDefinition> getById(@PathVariable Long id) {
        return ResponseResult.ok(flowDefinitionService.getById(id));
    }

    @PostMapping
    public ResponseResult<FlowDefinition> create(@RequestBody FlowDefinition entity) {
        entity.setId(null);
        if (entity.getStatus() == null) {
            entity.setStatus("draft");
        }
        if (entity.getVersion() == null) {
            entity.setVersion(1);
        }
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        flowDefinitionService.save(entity);
        return ResponseResult.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<FlowDefinition> update(@PathVariable Long id, @RequestBody FlowDefinition entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        flowDefinitionService.updateById(entity);
        return ResponseResult.ok(flowDefinitionService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(flowDefinitionService.removeById(id));
    }

    @PostMapping("/{id}/publish")
    public ResponseResult<Boolean> publish(@PathVariable Long id) {
        flowDefinitionService.publish(id);
        return ResponseResult.ok(true);
    }

    @PostMapping("/{id}/suspend")
    public ResponseResult<Boolean> suspend(@PathVariable Long id) {
        flowDefinitionService.suspend(id);
        return ResponseResult.ok(true);
    }
}
