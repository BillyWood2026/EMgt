package com.example.qualification.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.qualification.entity.QualificationStandard;
import com.example.qualification.service.QualificationStandardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 等级标准 (对应前端 qual-rank/Qualification.vue)
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/standard")
public class StandardController {

    @Autowired
    private QualificationStandardService standardService;

    /**
     * 等级标准列表(分页 + 名称/等级/状态过滤), 直接返回数组。
     * 前端 GET /api/standard/list?page=&size=&name=&level=&status=
     */
    @GetMapping("/list")
    public ResponseResult<List<QualificationStandard>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {

        QueryWrapper<QualificationStandard> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like("name", name.trim());
        }
        if (StringUtils.hasText(level)) {
            wrapper.eq("level", level.trim());
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status.trim());
        }
        wrapper.orderByDesc("create_time");

        Page<QualificationStandard> result = standardService.page(new Page<>(page, size), wrapper);
        return ResponseResult.success(result.getRecords());
    }

    /**
     * 查看标准详情
     */
    @GetMapping("/{id}")
    public ResponseResult<QualificationStandard> getById(@PathVariable Long id) {
        return ResponseResult.success(standardService.getById(id));
    }

    /**
     * 新增标准
     */
    @PostMapping
    public ResponseResult<QualificationStandard> create(@RequestBody QualificationStandard standard) {
        if (!StringUtils.hasText(standard.getStandardNo())) {
            standard.setStandardNo("STD-" + java.time.Year.now().getValue()
                    + "-" + String.valueOf(System.currentTimeMillis()).substring(10));
        }
        if (!StringUtils.hasText(standard.getStatus())) {
            standard.setStatus("启用");
        }
        standardService.save(standard);
        return ResponseResult.success(standard);
    }

    /**
     * 更新标准
     */
    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody QualificationStandard standard) {
        standard.setId(id);
        return ResponseResult.success(standardService.updateById(standard));
    }

    /**
     * 删除标准(逻辑删除)
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(standardService.removeById(id));
    }
}
