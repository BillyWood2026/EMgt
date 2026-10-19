package com.example.discipline.integrity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.discipline.common.ResponseResult;
import com.example.discipline.integrity.entity.IntegrityEducation;
import com.example.discipline.integrity.service.IntegrityEducationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/discipline/integrity")
@CrossOrigin(origins = "*")
public class IntegrityEducationController {

    @Autowired
    private IntegrityEducationService eduService;

    @GetMapping("/list")
    public ResponseResult<Page<IntegrityEducation>> list(@RequestParam(required = false) String eduNo,
                                                         @RequestParam(required = false) String eduType,
                                                         @RequestParam(required = false) String level,
                                                         @RequestParam(required = false) String status,
                                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResponseResult.success(eduService.page(eduNo, eduType, level, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseResult<IntegrityEducation> getById(@PathVariable Long id) {
        return ResponseResult.success(eduService.getById(id));
    }

    @PostMapping
    public ResponseResult<IntegrityEducation> create(@RequestBody IntegrityEducation e) {
        return ResponseResult.success(eduService.create(e));
    }

    @PutMapping("/{id}")
    public ResponseResult<IntegrityEducation> update(@PathVariable Long id, @RequestBody IntegrityEducation e) {
        return ResponseResult.success(eduService.update(id, e));
    }

    /** 开展活动：计划中 -> 进行中 */
    @PutMapping("/{id}/start")
    public ResponseResult<IntegrityEducation> start(@PathVariable Long id, @RequestBody(required = false) IntegrityEducation body) {
        return ResponseResult.success(eduService.start(id, body));
    }

    /** 参与/签到登记（进行中） */
    @PutMapping("/{id}/signin")
    public ResponseResult<IntegrityEducation> signin(@PathVariable Long id, @RequestBody(required = false) IntegrityEducation body) {
        return ResponseResult.success(eduService.signin(id, body));
    }

    /** 完成（宣传/承诺）：进行中 -> 已完成 */
    @PutMapping("/{id}/complete")
    public ResponseResult<IntegrityEducation> complete(@PathVariable Long id, @RequestBody(required = false) IntegrityEducation body) {
        return ResponseResult.success(eduService.complete(id, body));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(eduService.delete(id));
    }
}
