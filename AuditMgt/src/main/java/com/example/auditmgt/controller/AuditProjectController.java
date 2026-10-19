package com.example.auditmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auditmgt.common.ResponseResult;
import com.example.auditmgt.entity.AuditPlan;
import com.example.auditmgt.entity.AuditProject;
import com.example.auditmgt.mapper.AuditPlanMapper;
import com.example.auditmgt.mapper.AuditProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/audit/project")
@CrossOrigin(origins = "*")
public class AuditProjectController {

    @Autowired
    private AuditProjectMapper projectMapper;

    @Autowired
    private AuditPlanMapper planMapper;

    private void fillPlanName(AuditProject project) {
        if (project.getPlanId() != null) {
            AuditPlan plan = planMapper.selectById(project.getPlanId());
            if (plan != null && (project.getPlanName() == null || project.getPlanName().isEmpty())) {
                project.setPlanName(plan.getName());
            }
        }
    }

    @GetMapping("/list")
    public ResponseResult<Page<AuditProject>> list(@RequestParam(required = false) String name,
                                                   @RequestParam(required = false) Long planId,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<AuditProject> qw = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            qw.like(AuditProject::getName, name);
        }
        if (planId != null) {
            qw.eq(AuditProject::getPlanId, planId);
        }
        if (status != null && !status.isEmpty()) {
            qw.eq(AuditProject::getStatus, status);
        }
        qw.orderByDesc(AuditProject::getId);
        return ResponseResult.success(projectMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @GetMapping("/{id}")
    public ResponseResult<AuditProject> getById(@PathVariable Long id) {
        return ResponseResult.success(projectMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<AuditProject> create(@RequestBody AuditProject project) {
        if (project.getProjectNo() == null || project.getProjectNo().isEmpty()) {
            project.setProjectNo("PR" + System.currentTimeMillis());
        }
        if (project.getStatus() == null || project.getStatus().isEmpty()) {
            project.setStatus("preparing");
        }
        fillPlanName(project);
        project.setId(null);
        project.setDeleted(null);
        project.setCreateTime(LocalDateTime.now());
        project.setUpdateTime(LocalDateTime.now());
        projectMapper.insert(project);
        return ResponseResult.success(project);
    }

    @PutMapping("/{id}")
    public ResponseResult<AuditProject> update(@PathVariable Long id, @RequestBody AuditProject project) {
        project.setId(id);
        fillPlanName(project);
        project.setUpdateTime(LocalDateTime.now());
        projectMapper.updateById(project);
        return ResponseResult.success(projectMapper.selectById(id));
    }

    private ResponseResult<AuditProject> transit(Long id, String expected, String target, String action) {
        AuditProject project = projectMapper.selectById(id);
        if (project == null) {
            return ResponseResult.error("审计项目不存在");
        }
        if (!expected.equals(project.getStatus())) {
            return ResponseResult.error("非法状态流转：[" + project.getStatus() + "]不能执行[" + action + "]，需处于[" + expected + "]");
        }
        project.setStatus(target);
        project.setUpdateTime(LocalDateTime.now());
        projectMapper.updateById(project);
        return ResponseResult.success(project);
    }

    /** 组建组完成、进场实施：准备中 -> 现场审计 */
    @PutMapping("/{id}/start-field")
    public ResponseResult<AuditProject> startField(@PathVariable Long id) {
        return transit(id, "preparing", "field", "进场实施");
    }

    /** 现场审计结束、工作底稿归集：现场审计 -> 报告阶段 */
    @PutMapping("/{id}/start-reporting")
    public ResponseResult<AuditProject> startReporting(@PathVariable Long id) {
        return transit(id, "field", "reporting", "转入报告阶段");
    }

    /** 报告出具、项目归档：报告阶段 -> 已完成 */
    @PutMapping("/{id}/complete")
    public ResponseResult<AuditProject> complete(@PathVariable Long id) {
        return transit(id, "reporting", "completed", "项目结项");
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(projectMapper.deleteById(id) > 0);
    }
}
