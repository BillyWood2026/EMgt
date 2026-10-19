package com.example.orgmgt.controller;

import com.example.orgmgt.entity.JobDescription;
import com.example.orgmgt.service.JobDescriptionService;
import com.example.common.dto.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Job Description Controller
 * REST API endpoints for job description management
 */
@RestController
@RequestMapping("/api/job-description")
public class JobDescriptionController {
    
    @Autowired
    private JobDescriptionService jobDescriptionService;
    
    @GetMapping("/list")
    public ResponseResult<List<JobDescription>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String orgUnit,
            @RequestParam(required = false) String positionLevel,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        List<JobDescription> list = jobDescriptionService.getList(keyword, orgUnit, positionLevel, status, page, pageSize);
        return ResponseResult.success(list);
    }
    
    @GetMapping("/{id}")
    public ResponseResult<JobDescription> getById(@PathVariable Long id) {
        JobDescription jobDescription = jobDescriptionService.getById(id);
        return ResponseResult.success(jobDescription);
    }
    
    @GetMapping("/latest/{positionCode}")
    public ResponseResult<JobDescription> getLatestVersion(@PathVariable String positionCode) {
        JobDescription jobDescription = jobDescriptionService.getLatestVersion(positionCode);
        return ResponseResult.success(jobDescription);
    }
    
    @GetMapping("/versions/{positionCode}")
    public ResponseResult<List<JobDescription>> getAllVersions(@PathVariable String positionCode) {
        List<JobDescription> versions = jobDescriptionService.getAllVersions(positionCode);
        return ResponseResult.success(versions);
    }
    
    @PostMapping
    public ResponseResult<Void> create(@RequestBody JobDescription jobDescription) {
        jobDescriptionService.create(jobDescription);
        return ResponseResult.success();
    }
    
    @PutMapping("/{id}")
    public ResponseResult<Void> update(@PathVariable Long id, @RequestBody JobDescription jobDescription) {
        jobDescriptionService.update(id, jobDescription);
        return ResponseResult.success();
    }
    
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        jobDescriptionService.delete(id);
        return ResponseResult.success();
    }

    @PostMapping("/draft")
    public ResponseResult<Void> saveDraft(@RequestBody JobDescription jobDescription) {
        jobDescriptionService.saveDraft(jobDescription);
        return ResponseResult.success();
    }
    
    @PostMapping("/{id}/submit")
    public ResponseResult<Void> submitForApproval(@PathVariable Long id) {
        jobDescriptionService.submitForApproval(id);
        return ResponseResult.success();
    }
    
    @PostMapping("/{id}/approve")
    public ResponseResult<Void> approve(@PathVariable Long id, @RequestParam String approver) {
        jobDescriptionService.approve(id, approver);
        return ResponseResult.success();
    }
    
    @PostMapping("/{id}/reject")
    public ResponseResult<Void> reject(@PathVariable Long id, @RequestParam String approver, @RequestParam String reason) {
        jobDescriptionService.reject(id, approver, reason);
        return ResponseResult.success();
    }
    
    @PostMapping("/{id}/archive")
    public ResponseResult<Void> archive(@PathVariable Long id) {
        jobDescriptionService.archive(id);
        return ResponseResult.success();
    }
    
    @PostMapping("/{id}/abandon")
    public ResponseResult<Void> abandon(@PathVariable Long id) {
        jobDescriptionService.abandon(id);
        return ResponseResult.success();
    }
    
    @GetMapping("/{id}/preview")
    public ResponseResult<Map<String, Object>> preview(@PathVariable Long id) {
        Map<String, Object> preview = jobDescriptionService.preview(id);
        return ResponseResult.success(preview);
    }
    
    @GetMapping("/{id}/export/pdf")
    public ResponseResult<String> exportPdf(@PathVariable Long id) {
        String filePath = jobDescriptionService.exportPdf(id);
        return ResponseResult.success(filePath);
    }
    
    @GetMapping("/{id}/export/excel")
    public ResponseResult<String> exportExcel(@PathVariable Long id) {
        String filePath = jobDescriptionService.exportExcel(id);
        return ResponseResult.success(filePath);
    }
    
    @PostMapping("/import")
    public ResponseResult<Map<String, Object>> batchImport(@RequestParam String filePath) {
        Map<String, Object> result = jobDescriptionService.batchImport(filePath);
        return ResponseResult.success(result);
    }
    
    @PostMapping("/{sourceId}/copy")
    public ResponseResult<Void> copy(@PathVariable Long sourceId, @RequestParam String targetPositionCode) {
        jobDescriptionService.copy(sourceId, targetPositionCode);
        return ResponseResult.success();
    }
    
    @GetMapping("/{id}/history")
    public ResponseResult<List<Map<String, Object>>> getChangeHistory(@PathVariable Long id) {
        List<Map<String, Object>> history = jobDescriptionService.getChangeHistory(id);
        return ResponseResult.success(history);
    }
    
    @GetMapping("/{id}/compare/{versionNo}")
    public ResponseResult<Map<String, Object>> compareVersions(@PathVariable Long id, @PathVariable String versionNo) {
        Map<String, Object> diff = jobDescriptionService.compareVersions(id, versionNo);
        return ResponseResult.success(diff);
    }
    
    @GetMapping("/stats")
    public ResponseResult<Map<String, Integer>> getStats() {
        Map<String, Integer> stats = jobDescriptionService.getStats();
        return ResponseResult.success(stats);
    }
}