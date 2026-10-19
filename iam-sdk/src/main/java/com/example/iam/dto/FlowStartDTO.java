package com.example.iam.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class FlowStartDTO {
    
    private String flowId;
    private String moduleCode;
    private String flowType;
    
    private String businessKey;
    private String ticketNumber;
    private String description;
    
    private String operatorId;
    private String operatorName;
    private String operatorOrgId;
    
    private Map<String, Object> variables;
    
    private boolean skipFirstStep;
    private String callbackUrl;
    private String priority;
    private LocalDateTime expectedCompleteTime;
    
    private boolean crossModule;
    private String[] relatedModules;
    private String parentExecutionId;
}