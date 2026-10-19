package com.example.perf.controller;

import com.example.common.dto.ResponseResult;
import com.example.perf.entity.PerfTemplate;
import com.example.perf.service.PerfTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 兼容旧路径 /api/perf/kpi 的 Controller，底层复用 perf_template 真实持久层，
 * 保证旧端点不再因实体/表结构漂移而 500。
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/perf/kpi")
public class KpiController {

    @Autowired
    private PerfTemplateService perfTemplateService;

    @GetMapping("/list")
    public ResponseResult<List<PerfTemplate>> list(@RequestParam(required = false) String name,
                                                   @RequestParam(required = false) String dimension) {
        return ResponseResult.success(perfTemplateService.search(name, dimension));
    }

    @GetMapping("/{id}")
    public ResponseResult<PerfTemplate> getById(@PathVariable Long id) {
        return ResponseResult.success(perfTemplateService.getById(id));
    }

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

    @PutMapping("/{id}")
    public ResponseResult<PerfTemplate> update(@PathVariable Long id,
                                               @RequestBody PerfTemplate template) {
        template.setId(id);
        template.setUpdateTime(LocalDateTime.now());
        perfTemplateService.updateById(template);
        return ResponseResult.success(perfTemplateService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        perfTemplateService.removeById(id);
        return ResponseResult.success();
    }
}
