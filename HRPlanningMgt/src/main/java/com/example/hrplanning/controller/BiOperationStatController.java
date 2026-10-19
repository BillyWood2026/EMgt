package com.example.hrplanning.controller;

import com.example.common.dto.ResponseResult;
import com.example.hrplanning.entity.BiOperationStat;
import com.example.hrplanning.service.BiOperationStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BI 经营视图 /api/hrplanning/bi/stats
 * 前端 hr-planning/Index.vue「BI 经营视图」tab：
 *   GET /stats/period/{period} → 前端读取 res.data.data (即 ResponseResult.data)
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/hrplanning/bi/stats")
public class BiOperationStatController {

    @Autowired
    private BiOperationStatService biOperationStatService;

    @GetMapping("/period/{period}")
    public ResponseResult<List<BiOperationStat>> statsByPeriod(@PathVariable("period") String period) {
        return ResponseResult.success(biOperationStatService.listByPeriod(period));
    }

    // ---- 标准 CRUD ----

    @GetMapping("/list")
    public ResponseResult<List<BiOperationStat>> list() {
        return ResponseResult.success(biOperationStatService.list());
    }

    @GetMapping("/{id}")
    public ResponseResult<BiOperationStat> getById(@PathVariable Long id) {
        return ResponseResult.success(biOperationStatService.getById(id));
    }

    @PostMapping
    public ResponseResult<BiOperationStat> create(@RequestBody BiOperationStat stat) {
        biOperationStatService.save(stat);
        return ResponseResult.success(stat);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody BiOperationStat stat) {
        stat.setId(id);
        return ResponseResult.success(biOperationStatService.updateById(stat));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(biOperationStatService.removeById(id));
    }
}
