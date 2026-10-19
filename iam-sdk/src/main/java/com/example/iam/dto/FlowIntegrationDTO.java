package com.example.iam.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class FlowIntegrationDTO {
    
    private boolean success;
    private String executionId;
    private String flowId;
    private String flowName;
    private String currentNodeId;
    private String currentNodeName;
    private String status;
    private String message;
    private String errorCode;
    
    private Map<String, Object> variables;
    private List<ApproverInfo> currentApprovers;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationSeconds;
    
    private String operatorId;
    private String operatorName;
    private String operatorOrgId;
    private String comment;
    private String reason;
    
    private String moduleCode;
    private String businessKey;
    private String ticketNumber;
    
    @Data
    public static class TaskInfo {
        private String taskId;
        private String executionId;
        private String nodeId;
        private String nodeName;
        private String taskType;
        private String assigneeId;
        private String assigneeName;
        private String moduleCode;
        private String businessKey;
        private String ticketNumber;
        private String formTitle;
        private String formSummary;
        private String priority;
        private LocalDateTime createTime;
        private LocalDateTime expireTime;
        private LocalDateTime claimTime;
        private Long waitingSeconds;
        private boolean isOverdue;
        private Map<String, Object> variables;
        private List<ApproverInfo> candidateApprovers;
    }
    
    @Data
    public static class ApproverInfo {
        private String approverId;
        private String approverName;
        private String approverOrgId;
        private String approverOrgName;
        private String roleName;
        private String action;
        private LocalDateTime actionTime;
        private String comment;
    }
    
    @Data
    public static class ExecutionHistoryInfo {
        private String historyId;
        private String executionId;
        private String nodeId;
        private String nodeName;
        private String action;
        private String operatorId;
        private String operatorName;
        private String approverId;
        private String approverName;
        private String status;
        private String comment;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long durationSeconds;
        private Map<String, Object> variables;
    }
    
    @Data
    public static class FlowDefinitionInfo {
        private String flowId;
        private String flowName;
        private String version;
        private String moduleCode;
        private String flowType;
        private String description;
        private String status;
        private List<NodeInfo> nodes;
        private boolean crossModule;
        private LocalDateTime effectiveDate;
        private LocalDateTime expireDate;
    }
    
    @Data
    public static class NodeInfo {
        private String nodeId;
        private String nodeName;
        private String nodeType;
        private String assigneeType;
        private String assigneeValue;
        private Integer timeoutHours;
        private String timeoutAction;
        private Map<String, Object> properties;
    }
    
    public static FlowIntegrationDTO success(String executionId, String status) {
        FlowIntegrationDTO dto = new FlowIntegrationDTO();
        dto.setSuccess(true);
        dto.setExecutionId(executionId);
        dto.setStatus(status);
        dto.setMessage("Operation successful");
        return dto;
    }
    
    public static FlowIntegrationDTO fail(String errorCode, String message) {
        FlowIntegrationDTO dto = new FlowIntegrationDTO();
        dto.setSuccess(false);
        dto.setErrorCode(errorCode);
        dto.setMessage(message);
        dto.setStatus("FAILED");
        return dto;
    }
}