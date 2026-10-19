package com.example.recruitment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.dto.ResponseResult;
import com.example.recruitment.entity.RecruitmentRequirement;
import com.example.recruitment.service.RecruitmentRequirementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 招聘需求 REST 接口，契约对齐前端 Requirement.vue：
 * base /api/recruitment/requirement
 */
@RestController
@RequestMapping("/api/recruitment/requirement")
@CrossOrigin(origins = "*")
public class RecruitmentRequirementController {

    @Autowired
    private RecruitmentRequirementService requirementService;

    /**
     * 列表查询。可选参数：position（岗位名称，模糊）、department（部门，精确）、
     * status（状态，精确）；page/size 前端会传，列表直接返回数组，分页参数忽略。
     */
    @GetMapping("/list")
    public ResponseResult<List<RecruitmentRequirement>> list(
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<RecruitmentRequirement> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(position), RecruitmentRequirement::getPosition, position)
                .eq(StringUtils.hasText(department), RecruitmentRequirement::getDepartment, department)
                .eq(StringUtils.hasText(status), RecruitmentRequirement::getStatus, status)
                .orderByDesc(RecruitmentRequirement::getCreateTime)
                .orderByDesc(RecruitmentRequirement::getId);
        return ResponseResult.success(requirementService.list(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<RecruitmentRequirement> getById(@PathVariable Long id) {
        return ResponseResult.success(requirementService.getById(id));
    }

    @PostMapping
    public ResponseResult<RecruitmentRequirement> create(@RequestBody RecruitmentRequirement entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setId(null);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        requirementService.save(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id,
                                          @RequestBody RecruitmentRequirement entity) {
        entity.setId(id);
        RecruitmentRequirement existing = requirementService.getById(id);
        if (existing != null) {
            // 前端编辑表单不带 version，补齐当前版本号以保证乐观锁正常工作
            entity.setVersion(existing.getVersion());
        }
        entity.setUpdateTime(LocalDateTime.now());
        return ResponseResult.success(requirementService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(requirementService.removeById(id));
    }
}
