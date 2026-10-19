package com.example.flowengine.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.flowengine.common.ResponseResult;
import com.example.flowengine.entity.FlowTask;
import com.example.flowengine.service.FlowTaskService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flow/task")
public class FlowTaskController {

    private final FlowTaskService flowTaskService;

    public FlowTaskController(FlowTaskService flowTaskService) {
        this.flowTaskService = flowTaskService;
    }

    @GetMapping("/list")
    public ResponseResult<List<FlowTask>> list(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<FlowTask> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.and(k -> k.like(FlowTask::getNodeName, keyword).or().like(FlowTask::getAssignee, keyword));
        }
        w.orderByDesc(FlowTask::getId);
        return ResponseResult.ok(flowTaskService.list(w));
    }

    @GetMapping("/todo")
    public ResponseResult<List<FlowTask>> todo(@RequestParam String assignee) {
        return ResponseResult.ok(flowTaskService.list(new LambdaQueryWrapper<FlowTask>()
                .eq(FlowTask::getAssignee, assignee)
                .eq(FlowTask::getStatus, "pending")
                .orderByDesc(FlowTask::getId)));
    }

    @GetMapping("/done")
    public ResponseResult<List<FlowTask>> done(@RequestParam String assignee) {
        return ResponseResult.ok(flowTaskService.list(new LambdaQueryWrapper<FlowTask>()
                .eq(FlowTask::getAssignee, assignee)
                .ne(FlowTask::getStatus, "pending")
                .orderByDesc(FlowTask::getId)));
    }

    @GetMapping("/{id}")
    public ResponseResult<FlowTask> getById(@PathVariable Long id) {
        return ResponseResult.ok(flowTaskService.getById(id));
    }

    @PostMapping
    public ResponseResult<FlowTask> create(@RequestBody FlowTask entity) {
        entity.setId(null);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        flowTaskService.save(entity);
        return ResponseResult.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<FlowTask> update(@PathVariable Long id, @RequestBody FlowTask entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        flowTaskService.updateById(entity);
        return ResponseResult.ok(flowTaskService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(flowTaskService.removeById(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseResult<Boolean> complete(@PathVariable Long id, @RequestBody Map<String, String> body) {
        flowTaskService.complete(id, body.get("action"), body.get("comment"));
        return ResponseResult.ok(true);
    }
}
