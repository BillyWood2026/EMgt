package com.example.processmgt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.example.processmgt.entity.ProcessDefinition;
import com.example.processmgt.service.ProcessDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 流程定义 Controller
 */
@RestController
@RequestMapping("/api/process/def")
public class ProcessDefinitionController {

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    /**
     * 分页查询流程定义
     */
    @GetMapping("/list")
    public ResponseResult<IPage<ProcessDefinition>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        return ResponseResult.success(
                processDefinitionService.pageList(pageNum, pageSize, keyword, level, status, category));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public ResponseResult<ProcessDefinition> getById(@PathVariable Long id) {
        return ResponseResult.success(processDefinitionService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public ResponseResult<ProcessDefinition> create(@RequestBody ProcessDefinition processDefinition) {
        processDefinitionService.save(processDefinition);
        return ResponseResult.success(processDefinition);
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public ResponseResult<ProcessDefinition> update(@PathVariable Long id,
                                                     @RequestBody ProcessDefinition processDefinition) {
        processDefinition.setId(id);
        processDefinitionService.updateById(processDefinition);
        return ResponseResult.success(processDefinition);
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        processDefinitionService.removeById(id);
        return ResponseResult.success();
    }

    /**
     * 废止流程
     */
    @PutMapping("/{id}/deprecate")
    public ResponseResult<Void> deprecate(@PathVariable Long id) {
        processDefinitionService.deprecate(id);
        return ResponseResult.success();
    }

    /**
     * 归档流程
     */
    @PutMapping("/{id}/archive")
    public ResponseResult<Void> archive(@PathVariable Long id) {
        processDefinitionService.archive(id);
        return ResponseResult.success();
    }

    /**
     * 发布生效(仅评审通过的流程可发布，发布后纳入流程架构)
     */
    @PostMapping("/{id}/publish")
    public ResponseResult<ProcessDefinition> publish(@PathVariable Long id) {
        return ResponseResult.success(processDefinitionService.publish(id));
    }

    /**
     * 提交评审(draft/rejected -> pending)，同时生成评审审批记录
     */
    @PostMapping("/{id}/submit")
    public ResponseResult<ProcessDefinition> submit(@PathVariable Long id) {
        return ResponseResult.success(processDefinitionService.submitReview(id));
    }

    /**
     * 评审通过(pending -> approved)
     */
    @PostMapping("/{id}/approve")
    public ResponseResult<ProcessDefinition> approve(@PathVariable Long id,
                                                      @RequestBody(required = false) java.util.Map<String, String> body) {
        String comment = body == null ? null : body.get("comment");
        return ResponseResult.success(processDefinitionService.approve(id, comment));
    }

    /**
     * 评审驳回(pending -> rejected)
     */
    @PostMapping("/{id}/reject")
    public ResponseResult<ProcessDefinition> reject(@PathVariable Long id,
                                                     @RequestBody(required = false) java.util.Map<String, String> body) {
        String comment = body == null ? null : body.get("comment");
        return ResponseResult.success(processDefinitionService.reject(id, comment));
    }

    /**
     * 变更：基于已发布流程产生新版本草稿
     */
    @PostMapping("/{id}/new-version")
    public ResponseResult<ProcessDefinition> newVersion(@PathVariable Long id) {
        return ResponseResult.success(processDefinitionService.newVersion(id));
    }
}
