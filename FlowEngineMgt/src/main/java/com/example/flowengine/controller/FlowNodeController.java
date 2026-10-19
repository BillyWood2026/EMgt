package com.example.flowengine.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.flowengine.common.ResponseResult;
import com.example.flowengine.entity.FlowNode;
import com.example.flowengine.service.FlowNodeService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/flow/node")
public class FlowNodeController {

    private final FlowNodeService flowNodeService;

    public FlowNodeController(FlowNodeService flowNodeService) {
        this.flowNodeService = flowNodeService;
    }

    @GetMapping("/list")
    public ResponseResult<List<FlowNode>> list(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) Long defId) {
        LambdaQueryWrapper<FlowNode> w = new LambdaQueryWrapper<>();
        if (defId != null) {
            w.eq(FlowNode::getDefId, defId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            w.and(k -> k.like(FlowNode::getNodeName, keyword).or().like(FlowNode::getNodeKey, keyword));
        }
        w.orderByAsc(FlowNode::getSortOrder);
        return ResponseResult.ok(flowNodeService.list(w));
    }

    @GetMapping("/{id}")
    public ResponseResult<FlowNode> getById(@PathVariable Long id) {
        return ResponseResult.ok(flowNodeService.getById(id));
    }

    @PostMapping
    public ResponseResult<FlowNode> create(@RequestBody FlowNode entity) {
        entity.setId(null);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        flowNodeService.save(entity);
        return ResponseResult.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<FlowNode> update(@PathVariable Long id, @RequestBody FlowNode entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        flowNodeService.updateById(entity);
        return ResponseResult.ok(flowNodeService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(flowNodeService.removeById(id));
    }
}
