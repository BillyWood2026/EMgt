package com.example.auditmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auditmgt.common.ResponseResult;
import com.example.auditmgt.entity.AuditFinding;
import com.example.auditmgt.entity.AuditProject;
import com.example.auditmgt.entity.AuditReport;
import com.example.auditmgt.mapper.AuditFindingMapper;
import com.example.auditmgt.mapper.AuditProjectMapper;
import com.example.auditmgt.mapper.AuditReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit/report")
@CrossOrigin(origins = "*")
public class AuditReportController {

    @Autowired
    private AuditReportMapper reportMapper;

    @Autowired
    private AuditProjectMapper projectMapper;

    @Autowired
    private AuditFindingMapper findingMapper;

    private List<AuditFinding> findingsOf(Long projectId) {
        if (projectId == null) {
            return java.util.Collections.emptyList();
        }
        return findingMapper.selectList(new LambdaQueryWrapper<AuditFinding>()
                .eq(AuditFinding::getProjectId, projectId)
                .orderByAsc(AuditFinding::getId));
    }

    private void enrich(AuditReport report) {
        if (report.getProjectId() != null) {
            AuditProject project = projectMapper.selectById(report.getProjectId());
            if (project != null && (report.getProjectName() == null || report.getProjectName().isEmpty())) {
                report.setProjectName(project.getName());
            }
            report.setFindingsCount(findingsOf(report.getProjectId()).size());
        }
    }

    @GetMapping("/list")
    public ResponseResult<Page<AuditReport>> list(@RequestParam(required = false) String name,
                                                  @RequestParam(required = false) Long projectId,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<AuditReport> qw = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            qw.like(AuditReport::getName, name);
        }
        if (projectId != null) {
            qw.eq(AuditReport::getProjectId, projectId);
        }
        if (status != null && !status.isEmpty()) {
            qw.eq(AuditReport::getStatus, status);
        }
        qw.orderByDesc(AuditReport::getId);
        return ResponseResult.success(reportMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @GetMapping("/{id}")
    public ResponseResult<AuditReport> getById(@PathVariable Long id) {
        AuditReport report = reportMapper.selectById(id);
        if (report != null) {
            report.setFindings(findingsOf(report.getProjectId()));
        }
        return ResponseResult.success(report);
    }

    @PostMapping
    public ResponseResult<AuditReport> create(@RequestBody AuditReport report) {
        if (report.getReportNo() == null || report.getReportNo().isEmpty()) {
            report.setReportNo("AR" + System.currentTimeMillis());
        }
        if (report.getStatus() == null || report.getStatus().isEmpty()) {
            report.setStatus("draft");
        }
        if (report.getFindingsCount() == null) {
            report.setFindingsCount(0);
        }
        enrich(report);
        report.setId(null);
        report.setDeleted(null);
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        reportMapper.insert(report);
        report.setFindings(findingsOf(report.getProjectId()));
        return ResponseResult.success(report);
    }

    @PutMapping("/{id}")
    public ResponseResult<AuditReport> update(@PathVariable Long id, @RequestBody AuditReport report) {
        report.setId(id);
        enrich(report);
        report.setUpdateTime(LocalDateTime.now());
        reportMapper.updateById(report);
        AuditReport saved = reportMapper.selectById(id);
        if (saved != null) {
            saved.setFindings(findingsOf(saved.getProjectId()));
        }
        return ResponseResult.success(saved);
    }

    @PutMapping("/{id}/submit-review")
    public ResponseResult<AuditReport> submitReview(@PathVariable Long id) {
        AuditReport report = reportMapper.selectById(id);
        if (report == null) {
            return ResponseResult.error("审计报告不存在");
        }
        if (!"draft".equals(report.getStatus())) {
            return ResponseResult.error("非法状态流转：仅草稿(draft)可提交审核，当前[" + report.getStatus() + "]");
        }
        report.setStatus("reviewing");
        report.setUpdateTime(LocalDateTime.now());
        reportMapper.updateById(report);
        return ResponseResult.success(report);
    }

    @PutMapping("/{id}/publish")
    public ResponseResult<AuditReport> publish(@PathVariable Long id) {
        AuditReport report = reportMapper.selectById(id);
        if (report == null) {
            return ResponseResult.error("审计报告不存在");
        }
        if (!"reviewing".equals(report.getStatus())) {
            return ResponseResult.error("非法状态流转：仅待审核(reviewing)可出具发布，当前[" + report.getStatus() + "]");
        }
        report.setStatus("published");
        report.setReviewTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        reportMapper.updateById(report);
        return ResponseResult.success(report);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(reportMapper.deleteById(id) > 0);
    }
}
