package com.example.orgmgt.controller;

import com.example.orgmgt.entity.JobPositionLevel;
import com.example.orgmgt.entity.JobPositionChangeHistory;
import com.example.orgmgt.service.JobPositionLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Job Position Level Controller
 * REST API endpoints for five-level job position hierarchy management
 */
@RestController
@RequestMapping("/api/job-position")
public class JobPositionLevelController {
    
    @Autowired
    private JobPositionLevelService jobPositionLevelService;
    
    /**
     * Get position tree by organization
     */
    @GetMapping("/tree")
    public List<Map<String, Object>> getPositionTree() {
        return jobPositionLevelService.getPositionTree();
    }
    
    /**
     * Get position list with filters
     */
    @GetMapping("/list")
    public List<JobPositionLevel> getPositionList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer orgLevel,
            @RequestParam(required = false) String orgCode,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return jobPositionLevelService.getPositionList(keyword, orgLevel, orgCode, status, page, pageSize);
    }
    
    /**
     * Get position by ID
     */
    @GetMapping("/{id}")
    public JobPositionLevel getPositionById(@PathVariable Long id) {
        return jobPositionLevelService.getPositionById(id);
    }
    
    /**
     * Get position by code
     */
    @GetMapping("/code/{code}")
    public JobPositionLevel getPositionByCode(@PathVariable String code) {
        return jobPositionLevelService.getPositionByCode(code);
    }
    
    /**
     * Create new position (standard endpoint, initiator optional)
     */
    @PostMapping
    public Map<String, Object> createPositionRoot(@RequestBody JobPositionLevel position,
                                                  @RequestParam(required = false, defaultValue = "admin") String initiator) {
        return jobPositionLevelService.createPosition(position, initiator);
    }

    /**
     * Delete position (logical: abolish)
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deletePosition(@PathVariable Long id,
                                              @RequestParam(required = false, defaultValue = "DELETE") String reason,
                                              @RequestParam(required = false, defaultValue = "admin") String operator) {
        return jobPositionLevelService.abolishPosition(id, reason, operator);
    }

    /**
     * Create new position
     */
    @PostMapping("/create")
    public Map<String, Object> createPosition(@RequestBody JobPositionLevel position, 
                                               @RequestParam String initiator) {
        return jobPositionLevelService.createPosition(position, initiator);
    }
    
    /**
     * Update position
     */
    @PutMapping("/{id}")
    public Map<String, Object> updatePosition(@PathVariable Long id, 
                                               @RequestBody JobPositionLevel position,
                                               @RequestParam String operator) {
        return jobPositionLevelService.updatePosition(id, position, operator);
    }
    
    /**
     * Appoint person to position
     */
    @PostMapping("/{id}/appoint")
    public Map<String, Object> appointPerson(
            @PathVariable Long id,
            @RequestParam String personId,
            @RequestParam String personName,
            @RequestParam String appointReason,
            @RequestParam(required = false) String attachmentPath,
            @RequestParam String operator) {
        return jobPositionLevelService.appointPerson(id, personId, personName, appointReason, attachmentPath, operator);
    }
    
    /**
     * Abolish position
     */
    @PostMapping("/{id}/abolish")
    public Map<String, Object> abolishPosition(@PathVariable Long id, 
                                                @RequestParam String reason,
                                                @RequestParam String operator) {
        return jobPositionLevelService.abolishPosition(id, reason, operator);
    }
    
    /**
     * Toggle position status
     */
    @PostMapping("/{id}/toggle-status")
    public Map<String, Object> toggleStatus(@PathVariable Long id, 
                                            @RequestParam String newStatus,
                                            @RequestParam String operator) {
        return jobPositionLevelService.toggleStatus(id, newStatus, operator);
    }
    
    /**
     * Create personalized position
     */
    @PostMapping("/personalized")
    public Map<String, Object> createPersonalizedPosition(
            @RequestParam Long orgId,
            @RequestParam String personalizedName,
            @RequestParam String reason,
            @RequestParam String operator) {
        return jobPositionLevelService.createPersonalizedPosition(orgId, personalizedName, reason, operator);
    }
    
    /**
     * Validate position creation
     */
    @PostMapping("/validate")
    public Map<String, Object> validateCreate(@RequestBody JobPositionLevel position) {
        return jobPositionLevelService.validateCreate(position);
    }
    
    /**
     * Get standard positions for level
     */
    @GetMapping("/standard-positions/{level}")
    public List<String> getStandardPositions(@PathVariable Integer level) {
        return jobPositionLevelService.getStandardPositions(level);
    }
    
    /**
     * Get change history
     */
    @GetMapping("/history")
    public List<JobPositionChangeHistory> getChangeHistory(
            @RequestParam(required = false) String positionCode,
            @RequestParam(required = false) String changeType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return jobPositionLevelService.getChangeHistory(positionCode, changeType, page, pageSize);
    }
    
    /**
     * Get history by ID
     */
    @GetMapping("/history/{id}")
    public JobPositionChangeHistory getHistoryById(@PathVariable Long id) {
        return jobPositionLevelService.getHistoryById(id);
    }
    
    /**
     * Compare versions
     */
    @GetMapping("/history/compare")
    public Map<String, Object> compareVersions(
            @RequestParam Long historyId1,
            @RequestParam Long historyId2) {
        return jobPositionLevelService.compareVersions(historyId1, historyId2);
    }
    
    /**
     * Get position statistics
     */
    @GetMapping("/stats")
    public Map<String, Object> getPositionStats() {
        return jobPositionLevelService.getPositionStats();
    }
    
    /**
     * Batch export positions
     */
    @PostMapping("/batch-export")
    public List<JobPositionLevel> batchExport(@RequestBody List<Long> positionIds) {
        return jobPositionLevelService.batchExport(positionIds);
    }
    
    /**
     * Batch configure positions
     */
    @PostMapping("/batch-configure")
    public Map<String, Object> batchConfigure(
            @RequestParam Long orgId,
            @RequestBody List<JobPositionLevel> positions,
            @RequestParam String operator) {
        return jobPositionLevelService.batchConfigure(orgId, positions, operator);
    }
    
    /**
     * Sync to organization architecture
     */
    @PostMapping("/{id}/sync")
    public void syncToOrgArchitecture(@PathVariable Long id) {
        jobPositionLevelService.syncToOrgArchitecture(id);
    }
}