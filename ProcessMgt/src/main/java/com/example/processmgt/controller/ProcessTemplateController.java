package com.example.processmgt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.example.processmgt.entity.ProcessTemplate;
import com.example.processmgt.service.ProcessTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 流程模板 Controller
 */
@RestController
@RequestMapping("/api/process/template")
public class ProcessTemplateController {

    @Autowired
    private ProcessTemplateService processTemplateService;

    /**
     * 分页查询流程模板
     */
    @GetMapping("/list")
    public ResponseResult<IPage<ProcessTemplate>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        return ResponseResult.success(
                processTemplateService.pageList(pageNum, pageSize, keyword, category, status));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public ResponseResult<ProcessTemplate> getById(@PathVariable Long id) {
        return ResponseResult.success(processTemplateService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public ResponseResult<ProcessTemplate> create(@RequestBody ProcessTemplate processTemplate) {
        processTemplateService.save(processTemplate);
        return ResponseResult.success(processTemplate);
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public ResponseResult<ProcessTemplate> update(@PathVariable Long id,
                                                    @RequestBody ProcessTemplate processTemplate) {
        processTemplate.setId(id);
        processTemplateService.updateById(processTemplate);
        return ResponseResult.success(processTemplate);
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        processTemplateService.removeById(id);
        return ResponseResult.success();
    }
}
