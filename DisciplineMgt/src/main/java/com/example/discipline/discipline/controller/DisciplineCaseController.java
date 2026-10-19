package com.example.discipline.discipline.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.discipline.common.ResponseResult;
import com.example.discipline.discipline.entity.DisciplineCase;
import com.example.discipline.discipline.service.DisciplineCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/discipline/case")
@CrossOrigin(origins = "*")
public class DisciplineCaseController {

    @Autowired
    private DisciplineCaseService caseService;

    @GetMapping("/list")
    public ResponseResult<Page<DisciplineCase>> list(@RequestParam(required = false) String caseNo,
                                                     @RequestParam(required = false) String caseType,
                                                     @RequestParam(required = false) String caseLevel,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResponseResult.success(caseService.page(caseNo, caseType, caseLevel, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseResult<DisciplineCase> getById(@PathVariable Long id) {
        return ResponseResult.success(caseService.getById(id));
    }

    @PostMapping
    public ResponseResult<DisciplineCase> create(@RequestBody DisciplineCase c) {
        return ResponseResult.success(caseService.create(c));
    }

    @PutMapping("/{id}")
    public ResponseResult<DisciplineCase> update(@PathVariable Long id, @RequestBody DisciplineCase c) {
        return ResponseResult.success(caseService.update(id, c));
    }

    /** 线索登记 -> 初步核实 */
    @PutMapping("/{id}/verify")
    public ResponseResult<DisciplineCase> verify(@PathVariable Long id, @RequestBody(required = false) DisciplineCase body) {
        return ResponseResult.success(caseService.verify(id, body));
    }

    /** 初步核实 -> 立案 */
    @PutMapping("/{id}/file")
    public ResponseResult<DisciplineCase> file(@PathVariable Long id, @RequestBody(required = false) DisciplineCase body) {
        return ResponseResult.success(caseService.file(id, body));
    }

    /** 立案 -> 调查取证 */
    @PutMapping("/{id}/investigate")
    public ResponseResult<DisciplineCase> investigate(@PathVariable Long id, @RequestBody(required = false) DisciplineCase body) {
        return ResponseResult.success(caseService.investigate(id, body));
    }

    /** 调查取证 -> 审理 */
    @PutMapping("/{id}/review")
    public ResponseResult<DisciplineCase> review(@PathVariable Long id, @RequestBody(required = false) DisciplineCase body) {
        return ResponseResult.success(caseService.review(id, body));
    }

    /** 审理 -> 处分决定（须填写处分结果） */
    @PutMapping("/{id}/decide")
    public ResponseResult<DisciplineCase> decide(@PathVariable Long id, @RequestBody(required = false) DisciplineCase body) {
        return ResponseResult.success(caseService.decide(id, body));
    }

    /** 处分决定 -> 结案 */
    @PutMapping("/{id}/close")
    public ResponseResult<DisciplineCase> close(@PathVariable Long id, @RequestBody(required = false) DisciplineCase body) {
        return ResponseResult.success(caseService.close(id, body));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(caseService.delete(id));
    }
}
