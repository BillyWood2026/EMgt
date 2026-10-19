package com.example.orgmgt.service;

import com.example.orgmgt.entity.JobDescription;
import java.util.List;
import java.util.Map;

/**
 * Job Description Service Interface
 * Provides business logic for job description management
 */
public interface JobDescriptionService {
    
    /**
     * Get job description list with filters
     */
    List<JobDescription> getList(String keyword, String orgUnit, String positionLevel, String status, Integer page, Integer pageSize);
    
    /**
     * Get job description detail by ID
     */
    JobDescription getById(Long id);
    
    /**
     * Get latest version for a position
     */
    JobDescription getLatestVersion(String positionCode);
    
    /**
     * Get all versions for a position
     */
    List<JobDescription> getAllVersions(String positionCode);
    
    /**
     * Create new job description
     */
    void create(JobDescription jobDescription);

    /**
     * Delete job description by ID
     */
    void delete(Long id);
    
    /**
     * Update job description (trigger approval workflow)
     */
    void update(Long id, JobDescription jobDescription);
    
    /**
     * Save as draft
     */
    void saveDraft(JobDescription jobDescription);
    
    /**
     * Submit for approval
     * Approval flow: Dept Head Review -> HR Review -> Final Approval
     */
    void submitForApproval(Long id);
    
    /**
     * Approve job description
     */
    void approve(Long id, String approver);
    
    /**
     * Reject job description
     */
    void reject(Long id, String approver, String reason);
    
    /**
     * Archive job description
     */
    void archive(Long id);
    
    /**
     * Abandon job description
     */
    void abandon(Long id);
    
    /**
     * Preview job description
     */
    Map<String, Object> preview(Long id);
    
    /**
     * Export job description as PDF
     */
    String exportPdf(Long id);
    
    /**
     * Export job description as Excel
     */
    String exportExcel(Long id);
    
    /**
     * Batch import job descriptions
     */
    Map<String, Object> batchImport(String filePath);
    
    /**
     * Copy job description to another position
     */
    void copy(Long sourceId, String targetPositionCode);
    
    /**
     * Get change history for a job description
     */
    List<Map<String, Object>> getChangeHistory(Long id);
    
    /**
     * Compare two versions
     */
    Map<String, Object> compareVersions(Long id, String versionNo);
    
    /**
     * Get statistics
     */
    Map<String, Integer> getStats();
}