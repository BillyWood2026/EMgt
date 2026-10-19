package com.example.processmgt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.example.processmgt.entity.ProcessInstance;
import com.example.processmgt.service.ProcessInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 流程实例 Controller
 */
@RestController
@RequestMapping("/api/process/instance")
public class ProcessInstanceController {

    @Autowired
    private ProcessInstanceService processInstanceService;

    /**
     * 分页查询流程实例
     */
    @GetMapping("/list")
    public ResponseResult<IPage<ProcessInstance>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String applicant) {
        return ResponseResult.success(
                processInstanceService.pageList(pageNum, pageSize, keyword, status, applicant));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public ResponseResult<ProcessInstance> getById(@PathVariable Long id) {
        return ResponseResult.success(processInstanceService.getById(id));
    }

    /**
     * 新增(发起流程)
     */
    @PostMapping
    public ResponseResult<ProcessInstance> create(@RequestBody ProcessInstance processInstance) {
        return ResponseResult.success(processInstanceService.startProcess(processInstance));
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public ResponseResult<ProcessInstance> update(@PathVariable Long id,
                                                   @RequestBody ProcessInstance processInstance) {
        processInstance.setId(id);
        processInstanceService.updateById(processInstance);
        return ResponseResult.success(processInstance);
    }

    /**
     * 删除(逻辑删除)
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        processInstanceService.removeById(id);
        return ResponseResult.success();
    }

    /**
     * 取消流程
     */
    @PutMapping("/{id}/cancel")
    public ResponseResult<Void> cancel(@PathVariable Long id) {
        processInstanceService.cancel(id);
        return ResponseResult.success();
    }

    /**
     * 执行流程监控流转：推进节点 / 办结
     * body: {"currentNode":"部门审批","complete":false}
     */
    @PostMapping("/{id}/execute")
    public ResponseResult<ProcessInstance> execute(@PathVariable Long id,
                                                    @RequestBody(required = false) java.util.Map<String, Object> body) {
        String node = null;
        boolean complete = false;
        if (body != null) {
            Object n = body.get("currentNode");
            if (n != null) {
                node = String.valueOf(n);
            }
            Object c = body.get("complete");
            if (c != null) {
                complete = Boolean.parseBoolean(String.valueOf(c));
            }
        }
        return ResponseResult.success(processInstanceService.execute(id, node, complete));
    }
}
