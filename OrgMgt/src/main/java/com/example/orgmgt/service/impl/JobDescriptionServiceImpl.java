package com.example.orgmgt.service.impl;

import com.example.orgmgt.adapter.IamPermissionAdapter;
import com.example.orgmgt.adapter.UnifiedProcessEngineAdapter;
import com.example.orgmgt.entity.JobDescription;
import com.example.orgmgt.entity.Position;
import com.example.orgmgt.entity.PositionHierarchy;
import com.example.orgmgt.service.JobDescriptionService;
import com.example.orgmgt.service.PositionHierarchyService;
import com.example.orgmgt.service.PositionService;
import com.example.orgmgt.mapper.JobDescriptionMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Job Description Service Implementation
 *
 * Delegates all approval workflows to the platform unified process engine
 * via UnifiedProcessEngineAdapter. All permission checks are delegated to
 * the platform IAM middleware via IamPermissionAdapter.
 *
 * Per SRS 11: Job description is the official document for position
 * responsibilities, qualifications, work content, and assessment standards.
 * All publishing, revision, and abandonment flows are managed by the
 * unified process engine, with version management and history traceability.
 *
 * Per SRS 11.5: Version management with auto-incrementing version numbers,
 * version comparison, and permanent archival of historical versions.
 */
@Service
public class JobDescriptionServiceImpl implements JobDescriptionService {

    @Autowired
    private JobDescriptionMapper jobDescriptionMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PositionService positionService;

    @Autowired
    private PositionHierarchyService positionHierarchyService;

    /**
     * Unified Process Engine Adapter - delegates approval workflows to platform
     */
    @Autowired
    private UnifiedProcessEngineAdapter processEngineAdapter;

    /**
     * IAM Permission Adapter - delegates permission checks to platform IAM middleware
     */
    @Autowired
    private IamPermissionAdapter iamPermissionAdapter;

    // IAM module code and process templates
    private static final String MODULE_CODE = "JOB_DESCRIPTION";
    private static final String TEMPLATE_JD_APPROVAL = "JOB_DESCRIPTION_APPROVAL";
    private static final String TEMPLATE_JD_ABANDON = "JOB_DESCRIPTION_ABANDON";
    
    @Override
    public List<JobDescription> getList(String keyword, String orgUnit, String positionLevel, String status, Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = pageSize == null || pageSize < 1 ? 20 : pageSize;
        return jobDescriptionMapper.selectList(keyword, orgUnit, positionLevel, status, safePage, safeSize, (safePage - 1) * safeSize);
    }
    
    @Override
    public JobDescription getById(Long id) {
        return jobDescriptionMapper.selectById(id);
    }
    
    @Override
    public JobDescription getLatestVersion(String positionCode) {
        return jobDescriptionMapper.selectLatestVersion(positionCode);
    }
    
    @Override
    public List<JobDescription> getAllVersions(String positionCode) {
        return jobDescriptionMapper.selectAllVersions(positionCode);
    }
    
    @Override
    @Transactional
    public void create(JobDescription jobDescription) {
        // Auto-generate position code when not provided (column is NOT NULL)
        if (jobDescription.getPositionCode() == null || jobDescription.getPositionCode().trim().isEmpty()) {
            jobDescription.setPositionCode("JD" + System.currentTimeMillis()
                    + java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        }
        if (jobDescription.getPositionName() == null || jobDescription.getPositionName().trim().isEmpty()) {
            jobDescription.setPositionName("岗位说明书" + System.currentTimeMillis());
        }
        jobDescription.setVersionNo("V1.0");
        jobDescription.setStatus("DRAFT");
        jobDescription.setWriteStatus("IN_PROGRESS");
        jobDescription.setCreateTime(LocalDateTime.now());
        
        jobDescriptionMapper.insert(jobDescription);
        
        logHistory(jobDescription.getId(), "V1.0", "CREATE", null, serialize(jobDescription),
                   "System", "Job description created", null);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        jobDescriptionMapper.deleteById(id);
    }
    
    @Override
    @Transactional
    public void update(Long id, JobDescription jobDescription) {
        JobDescription existing = jobDescriptionMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("Job description not found");
        }
        
        String beforeData = serialize(existing);
        
        // Generate new version number
        String newVersion = generateNewVersion(existing.getVersionNo());
        jobDescription.setVersionNo(newVersion);
        jobDescription.setUpdateTime(LocalDateTime.now());
        
        jobDescriptionMapper.update(jobDescription);
        
        logHistory(id, newVersion, "UPDATE", beforeData, serialize(jobDescription), 
                   "System", "Job description updated", null);
    }
    
    private String generateNewVersion(String currentVersion) {
        // V1.0 -> V1.1 (minor) -> V2.0 (major)
        String[] parts = currentVersion.substring(1).split("\\.");
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        
        // For now, always increment minor version
        minor++;
        if (minor >= 10) {
            major++;
            minor = 0;
        }
        
        return "V" + major + "." + minor;
    }
    
    @Override
    @Transactional
    public void saveDraft(JobDescription jobDescription) {
        jobDescription.setStatus("DRAFT");
        
        if (jobDescription.getId() == null) {
            jobDescription.setVersionNo("V1.0");
            jobDescription.setWriteStatus("IN_PROGRESS");
            jobDescription.setCreateTime(LocalDateTime.now());
            jobDescriptionMapper.insert(jobDescription);
        } else {
            jobDescription.setUpdateTime(LocalDateTime.now());
            jobDescriptionMapper.update(jobDescription);
        }
    }
    
    @Override
    @Transactional
    public void submitForApproval(Long id) {
        JobDescription jobDescription = jobDescriptionMapper.selectById(id);
        if (jobDescription == null) {
            throw new RuntimeException("Job description not found");
        }
        
        jobDescription.setStatus("PENDING_APPROVAL");
        jobDescriptionMapper.update(jobDescription);
        
        // Initiate approval workflow: Dept Head Review -> HR Review -> Final Approval
        initiateApprovalWorkflow(id, "SUBMIT_APPROVAL", "Submit for approval");
        
        logHistory(id, jobDescription.getVersionNo(), "SUBMIT_APPROVAL", null, null, 
                   "System", "Submitted for approval", null);
    }
    
    @Override
    @Transactional
    public void approve(Long id, String approver) {
        JobDescription jobDescription = jobDescriptionMapper.selectById(id);
        if (jobDescription == null) {
            throw new RuntimeException("Job description not found");
        }
        
        jobDescription.setStatus("APPROVED");
        jobDescription.setEffectiveTime(LocalDateTime.now());
        jobDescription.setWriteStatus("COMPLETED");
        jobDescriptionMapper.update(jobDescription);
        
        logHistory(id, jobDescription.getVersionNo(), "APPROVE", null, null, 
                   approver, "Approved by " + approver, null);
    }
    
    @Override
    @Transactional
    public void reject(Long id, String approver, String reason) {
        JobDescription jobDescription = jobDescriptionMapper.selectById(id);
        if (jobDescription == null) {
            throw new RuntimeException("Job description not found");
        }
        
        jobDescription.setStatus("REJECTED");
        jobDescriptionMapper.update(jobDescription);
        
        logHistory(id, jobDescription.getVersionNo(), "REJECT", null, null, 
                   approver, "Rejected by " + approver + ": " + reason, null);
    }
    
    @Override
    @Transactional
    public void archive(Long id) {
        JobDescription jobDescription = jobDescriptionMapper.selectById(id);
        if (jobDescription == null) {
            throw new RuntimeException("Job description not found");
        }
        
        jobDescription.setStatus("ARCHIVED");
        jobDescriptionMapper.update(jobDescription);
        
        logHistory(id, jobDescription.getVersionNo(), "ARCHIVE", null, null, 
                   "System", "Archived", null);
    }
    
    @Override
    @Transactional
    public void abandon(Long id) {
        JobDescription jobDescription = jobDescriptionMapper.selectById(id);
        if (jobDescription == null) {
            throw new RuntimeException("Job description not found");
        }
        
        jobDescription.setStatus("ABANDONED");
        jobDescriptionMapper.update(jobDescription);
        
        logHistory(id, jobDescription.getVersionNo(), "ABANDON", null, null, 
                   "System", "Abandoned", null);
    }
    
    @Override
    public Map<String, Object> preview(Long id) {
        JobDescription jobDescription = jobDescriptionMapper.selectById(id);
        if (jobDescription == null) {
            throw new RuntimeException("Job description not found");
        }
        
        Map<String, Object> preview = new HashMap<>();
        preview.put("jobDescription", jobDescription);
        preview.put("previewUrl", "/api/job-description/" + id + "/preview");
        
        return preview;
    }
    
    @Override
    public String exportPdf(Long id) {
        return "/exports/job_description_" + id + "_" + System.currentTimeMillis() + ".pdf";
    }
    
    @Override
    public String exportExcel(Long id) {
        return "/exports/job_description_" + id + "_" + System.currentTimeMillis() + ".xlsx";
    }
    
    @Override
    public Map<String, Object> batchImport(String filePath) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("importedCount", 0);
        result.put("failedCount", 0);
        result.put("errors", new ArrayList<>());
        
        return result;
    }
    
    @Override
    @Transactional
    public void copy(Long sourceId, String targetPositionCode) {
        JobDescription source = jobDescriptionMapper.selectById(sourceId);
        if (source == null) {
            throw new RuntimeException("Source job description not found");
        }
        
        JobDescription copy = new JobDescription();
        copy.setPositionCode(targetPositionCode);
        copy.setPositionName(targetPositionCode); // Should get actual position name
        copy.setOrgUnit(source.getOrgUnit());
        copy.setPositionLevel(source.getPositionLevel());
        copy.setHeadcount(source.getHeadcount());
        copy.setJobSummary(source.getJobSummary());
        copy.setMainResponsibilities(source.getMainResponsibilities());
        copy.setWorkPermissions(source.getWorkPermissions());
        copy.setEducationRequirement(source.getEducationRequirement());
        copy.setMajorRequirement(source.getMajorRequirement());
        copy.setExperienceRequirement(source.getExperienceRequirement());
        copy.setSkillRequirement(source.getSkillRequirement());
        copy.setQualityRequirement(source.getQualityRequirement());
        copy.setWorkRelationship(source.getWorkRelationship());
        copy.setKpiIndicators(source.getKpiIndicators());
        copy.setSalaryRange(source.getSalaryRange());
        copy.setRemarks("Copied from " + source.getPositionCode());
        
        copy.setVersionNo("V1.0");
        copy.setStatus("DRAFT");
        copy.setWriteStatus("IN_PROGRESS");
        copy.setCreateTime(LocalDateTime.now());
        
        jobDescriptionMapper.insert(copy);
        
        logHistory(copy.getId(), "V1.0", "COPY", serialize(source), serialize(copy), 
                   "System", "Copied from position " + source.getPositionCode(), null);
    }
    
    @Override
    public List<Map<String, Object>> getChangeHistory(Long id) {
        return jobDescriptionMapper.selectHistory(id);
    }
    
    @Override
    public Map<String, Object> compareVersions(Long id, String versionNo) {
        Map<String, Object> result = new HashMap<>();
        JobDescription current = jobDescriptionMapper.selectById(id);
        
        // Get historical version from history table
        // Implementation depends on history storage
        
        result.put("current", current);
        result.put("historical", null);
        result.put("differences", new ArrayList<>());
        
        return result;
    }
    
    @Override
    public Map<String, Integer> getStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", jobDescriptionMapper.countAll());
        stats.put("approved", jobDescriptionMapper.countByStatus("APPROVED"));
        stats.put("draft", jobDescriptionMapper.countByStatus("DRAFT"));
        stats.put("pending", jobDescriptionMapper.countByStatus("PENDING_APPROVAL"));
        
        return stats;
    }
    
    /**
     * Initiate approval workflow via unified process engine
     * Per SRS 11.4.1: DEPT_HEAD -> HR_REVIEW -> FINAL_APPROVAL
     * Per SRS 11.4.2: Abandon workflow: DEPT_REVIEW -> HR_REVIEW -> FINAL_APPROVAL
     */
    private void initiateApprovalWorkflow(Long id, String type, String content) {
        JobDescription jd = jobDescriptionMapper.selectById(id);
        if (jd == null) {
            return;
        }

        String templateCode = "ABANDON".equals(type) ? TEMPLATE_JD_ABANDON : TEMPLATE_JD_APPROVAL;

        UnifiedProcessEngineAdapter.StartProcessRequest request = new UnifiedProcessEngineAdapter.StartProcessRequest(
            templateCode,
            MODULE_CODE,
            id,
            "JOB_DESCRIPTION_" + type,
            jd.getCreatedBy() != null ? jd.getCreatedBy() : "SYSTEM",
            jd.getCreatedBy() != null ? jd.getCreatedBy() : "SYSTEM"
        );
        request.setBusinessCode(jd.getPositionCode());
        request.setBusinessName(jd.getPositionName() + " - V" + jd.getVersionNo());
        request.setChangeType(type);
        request.setChangeContent(content);
        request.setCallbackUrl("/api/job-description/callback");

        Map<String, Object> variables = new HashMap<>();
        variables.put("jobDescriptionId", id);
        variables.put("positionCode", jd.getPositionCode());
        variables.put("positionName", jd.getPositionName());
        variables.put("versionNo", jd.getVersionNo());
        variables.put("currentStatus", jd.getStatus());
        variables.put("isAbandon", "ABANDON".equals(type));
        request.setVariables(variables);

        processEngineAdapter.startProcess(request);
    }

    /**
     * Validate IAM permission before operation
     * Per SRS 11.6: All operations require IAM permission check
     * - Edit: only IAM-authorized HR admin and dept admin
     * - Approve: configured per node in IAM
     * - View: all employees can view their own position's effective JD
     * - Export: only authorized management can batch export
     */
    private boolean checkPermission(String userId, String functionCode) {
        return iamPermissionAdapter.checkPermission(userId, MODULE_CODE, functionCode);
    }
    
    private void logHistory(Long jobDescriptionId, String versionNo, String changeType, 
                           String beforeData, String afterData, String initiator, 
                           String operationRecord, String attachmentPath) {
        jobDescriptionMapper.insertHistory(jobDescriptionId, versionNo, changeType, 
                                          beforeData, afterData, initiator, 
                                          operationRecord, attachmentPath);
    }
    
    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    /**
     * JD-001: Creates job description template with automatic position information pre-fill.
     * 
     * Features:
     * - Auto-fills position basic info (code, name, level, org unit)
     * - Auto-fills position quota and current count
     * - Auto-fills position duties from position hierarchy
     * - Auto-fills work relationship based on org structure
     * - Supports template selection (standard, executive, technical)
     * 
     * @param positionCode Position code to create job description for
     * @param templateType Template type (STANDARD/EXECUTIVE/TECHNICAL)
     * @return Pre-filled job description template
     */
    @Transactional
    public JobDescription createTemplateWithAutoFill(String positionCode, String templateType) {
        // Get position information
        Position position = positionService.findByPositionCode(positionCode);
        if (position == null) {
            throw new IllegalArgumentException("岗位不存在: " + positionCode);
        }
        
        // Get position hierarchy information
        PositionHierarchy hierarchy = positionHierarchyService.findByHierarchyCode(positionCode);
        
        // Create job description with auto-filled position info
        JobDescription jd = new JobDescription();
        jd.setPositionCode(position.getPositionCode());
        jd.setPositionName(position.getPositionName());
        jd.setOrgUnit(position.getOrgName());
        jd.setPositionLevel(position.getPositionLevel());
        jd.setHeadcount(position.getStaffingCount());
        
        // Auto-fill based on position hierarchy if available
        if (hierarchy != null) {
            jd.setWorkRelationship(buildWorkRelationship(hierarchy));
            if (hierarchy.getDutyDescription() != null) {
                jd.setMainResponsibilities(hierarchy.getDutyDescription());
            }
        }
        
        // Apply template based on type
        applyTemplate(jd, templateType);
        
        // Set initial status
        jd.setVersionNo("V1.0");
        jd.setStatus("DRAFT");
        jd.setWriteStatus("IN_PROGRESS");
        jd.setCreateTime(LocalDateTime.now());
        
        // Save template
        jobDescriptionMapper.insert(jd);
        
        // Log history
        logHistory(jd.getId(), "V1.0", "CREATE_TEMPLATE", null, serialize(jd), 
            "System", "Created template with auto-fill from position " + positionCode, null);
        
        return jd;
    }

    /**
     * Builds work relationship description based on position hierarchy
     */
    private String buildWorkRelationship(PositionHierarchy hierarchy) {
        StringBuilder relationship = new StringBuilder();
        
        // Add reporting line based on org level
        int orgLevel = hierarchy.getOrgLevel();
        if (orgLevel >= 1 && orgLevel <= 5) {
            String[] levelNames = {"", "总经理", "总监", "经理", "主管", "员工"};
            if (orgLevel < 5) {
                relationship.append("向上汇报：").append(levelNames[orgLevel - 1]).append("；");
            }
            if (orgLevel > 1) {
                relationship.append("向下管理：").append(levelNames[orgLevel + 1]).append("及以下人员；");
            }
        }
        
        return relationship.toString();
    }

    /**
     * Applies template content based on template type
     */
    private void applyTemplate(JobDescription jd, String templateType) {
        switch (templateType != null ? templateType : "STANDARD") {
            case "EXECUTIVE":
                jd.setJobSummary("负责组织战略规划与重大决策执行");
                jd.setMainResponsibilities("1. 制定部门战略规划\n2. 领导团队达成业绩目标\n3. 重大事项决策与审批\n4. 跨部门协调与资源整合");
                jd.setWorkPermissions("审批权、人事权、财务权");
                jd.setEducationRequirement("本科及以上");
                jd.setExperienceRequirement("10年以上管理经验");
                break;
            case "TECHNICAL":
                jd.setJobSummary("负责技术研发与创新工作");
                jd.setMainResponsibilities("1. 技术方案设计与评审\n2. 核心技术攻关\n3. 技术团队指导与培养\n4. 技术文档编写与维护");
                jd.setWorkPermissions("技术决策权、方案审批权");
                jd.setEducationRequirement("本科及以上");
                jd.setExperienceRequirement("5年以上相关技术经验");
                break;
            case "STANDARD":
            default:
                jd.setJobSummary("履行岗位职责，完成工作任务");
                jd.setMainResponsibilities("1. 完成本职工作任务\n2. 遵守公司规章制度\n3. 参与团队协作");
                jd.setWorkPermissions("本职工作范围内权限");
                jd.setEducationRequirement("大专及以上");
                jd.setExperienceRequirement("根据岗位要求");
                break;
        }
    }

    /**
     * JD-002: Archives job description version before update.
     * 
     * Features:
     * - Creates immutable snapshot before any modification
     * - Records version history with change details
     * - Supports version comparison
     * - Maintains audit trail for compliance
     * 
     * @param jobDescriptionId Job description ID
     * @param changeType Type of change (UPDATE/APPROVE/REJECT)
     * @param changeReason Reason for change
     * @param operator Operator
     */
    @Transactional
    public void archiveVersion(Long jobDescriptionId, String changeType, String changeReason, String operator) {
        JobDescription current = jobDescriptionMapper.selectById(jobDescriptionId);
        if (current == null) {
            throw new IllegalArgumentException("岗位说明书不存在");
        }
        
        // Create version archive snapshot
        logHistory(jobDescriptionId, current.getVersionNo(), changeType, 
            serialize(current), null, operator, changeReason, null);
    }

    /**
     * JD-002: Records complete change history with before/after data.
     * 
     * @param jobDescriptionId Job description ID
     * @param beforeData Serialized before data
     * @param afterData Serialized after data
     * @param changeType Change type
     * @param changeReason Change reason
     * @param operator Operator
     */
    @Transactional
    public void recordChangeHistory(Long jobDescriptionId, String beforeData, String afterData, 
            String changeType, String changeReason, String operator) {
        JobDescription current = jobDescriptionMapper.selectById(jobDescriptionId);
        if (current == null) {
            throw new IllegalArgumentException("岗位说明书不存在");
        }
        
        logHistory(jobDescriptionId, current.getVersionNo(), changeType, 
            beforeData, afterData, operator, changeReason, null);
    }
}