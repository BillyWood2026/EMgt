package com.example.perf.controller;

import com.example.common.dto.ResponseResult;
import com.example.perf.entity.PerfTemplate;
import com.example.perf.service.PerfTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * KPI指标管理 Controller（前端 Kpi.vue，base /api/perf/template）
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/perf/template")
public class PerfTemplateController {

    @Autowired
    private PerfTemplateService perfTemplateService;

    /**
     * 列表查询：可选 name（模糊）、dimension（精确）
     */
    @GetMapping("/list")
    public ResponseResult<List<PerfTemplate>> list(@RequestParam(required = false) String name,
                                                   @RequestParam(required = false) String dimension) {
        return ResponseResult.success(perfTemplateService.search(name, dimension));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public ResponseResult<PerfTemplate> getById(@PathVariable Long id) {
        return ResponseResult.success(perfTemplateService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public ResponseResult<PerfTemplate> create(@RequestBody PerfTemplate template) {
        LocalDateTime now = LocalDateTime.now();
        template.setId(null);
        template.setDeleted(0);
        template.setVersion(1);
        template.setCreateTime(now);
        template.setUpdateTime(now);
        perfTemplateService.save(template);
        return ResponseResult.success(template);
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public ResponseResult<PerfTemplate> update(@PathVariable Long id,
                                               @RequestBody PerfTemplate template) {
        template.setId(id);
        template.setUpdateTime(LocalDateTime.now());
        perfTemplateService.updateById(template);
        return ResponseResult.success(perfTemplateService.getById(id));
    }

    /**
     * 删除（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        perfTemplateService.removeById(id);
        return ResponseResult.success();
    }
}
