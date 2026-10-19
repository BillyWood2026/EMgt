package com.example.skillcert.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.example.skillcert.entity.SkillPlan;
import com.example.skillcert.service.SkillPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证计划（skill-certification/SkillPlan.vue）
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/skill-cert/plan")
public class SkillPlanController {

    @Autowired
    private SkillPlanService skillPlanService;

    @GetMapping("/list")
    public ResponseResult<IPage<SkillPlan>> list(@RequestParam(defaultValue = "1") long current,
                                                   @RequestParam(defaultValue = "20") long size,
                                                   @RequestParam(required = false) String planStatus,
                                                   @RequestParam(required = false) Integer planYear) {
        return ResponseResult.success(skillPlanService.pageList(current, size, planStatus, planYear));
    }

    @GetMapping("/{id}")
    public ResponseResult<SkillPlan> getById(@PathVariable Long id) {
        return ResponseResult.success(skillPlanService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody SkillPlan plan) {
        return ResponseResult.success(skillPlanService.save(plan));
    }

    /** 更新（id 在请求体中，路径无 /{id}） */
    @PutMapping
    public ResponseResult<Boolean> updateRoot(@RequestBody SkillPlan plan) {
        return ResponseResult.success(skillPlanService.updateById(plan));
    }

    /** 更新（id 在路径中，Vue 使用） */
    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody SkillPlan plan) {
        plan.setId(id);
        return ResponseResult.success(skillPlanService.updateById(plan));
    }

    @PutMapping("/{id}/terminate")
    public ResponseResult<Boolean> terminate(@PathVariable Long id) {
        return ResponseResult.success(skillPlanService.terminate(id));
    }

    @PutMapping("/{id}/archive")
    public ResponseResult<Boolean> archive(@PathVariable Long id) {
        return ResponseResult.success(skillPlanService.archive(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(skillPlanService.removeById(id));
    }
}
