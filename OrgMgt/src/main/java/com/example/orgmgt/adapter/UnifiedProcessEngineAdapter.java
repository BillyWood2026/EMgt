package com.example.orgmgt.adapter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Unified Process Engine Adapter Interface
 *
 * Delegates all approval workflows to the platform unified process engine.
 * The OrgMgt module does NOT self-build any process rules, approval nodes,
 * or workflow logic. All approval flows are configured and managed by the
 * unified process engine.
 *
 * Capabilities provided by the unified process engine:
 * - Process template configuration (node-based, free-flow, conditional)
 * - Approval actions: approve, reject, recall, add-sign, cc, terminate
 * - Process callbacks to business interfaces after approval completion
 * - Full audit trail with operator, time, opinion, IP address
 * - Dynamic template adjustment without code changes
 */
public interface UnifiedProcessEngineAdapter {

    /**
     * Start a new approval process instance
     *
     * @param request Process start request
     * @return Process instance info
     */
    ProcessInstance startProcess(StartProcessRequest request);

    /**
     * Approve current node
     *
     * @param processInstanceId Process instance ID
     * @param approver Approver user ID
     * @param opinion Approval opinion
     * @param ipAddress Operator IP address
     * @return Updated process instance
     */
    ProcessInstance approve(String processInstanceId, String approver, String opinion, String ipAddress);

    /**
     * Reject current node (returns to initiator for modification)
     *
     * @param processInstanceId Process instance ID
     * @param approver Approver user ID
     * @param rejectReason Reject reason
     * @param ipAddress Operator IP address
     * @return Updated process instance
     */
    ProcessInstance reject(String processInstanceId, String approver, String rejectReason, String ipAddress);

    /**
     * Recall process (only allowed for un-finalized instances)
     *
     * @param processInstanceId Process instance ID
     * @param operator Operator user ID
     * @param ipAddress Operator IP address
     * @return Updated process instance
     */
    ProcessInstance recall(String processInstanceId, String operator, String ipAddress);

    /**
     * Add additional approver to current node
     *
     * @param processInstanceId Process instance ID
     * @param operator Operator user ID
     * @param additionalApprover Additional approver user ID
     * @param opinion Add-sign opinion
     * @param ipAddress Operator IP address
     * @return Updated process instance
     */
    ProcessInstance addSign(String processInstanceId, String operator, String additionalApprover,
                             String opinion, String ipAddress);

    /**
     * CC (carbon copy) to other users
     *
     * @param processInstanceId Process instance ID
     * @param operator Operator user ID
     * @param ccUsers CC user IDs
     * @param ipAddress Operator IP address
     * @return Updated process instance
     */
    ProcessInstance cc(String processInstanceId, String operator, List<String> ccUsers, String ipAddress);

    /**
     * Terminate process
     *
     * @param processInstanceId Process instance ID
     * @param operator Operator user ID
     * @param terminateReason Terminate reason
     * @param ipAddress Operator IP address
     * @return Updated process instance
     */
    ProcessInstance terminate(String processInstanceId, String operator, String terminateReason, String ipAddress);

    /**
     * Get process instance by ID
     *
     * @param processInstanceId Process instance ID
     * @return Process instance info
     */
    ProcessInstance getProcessInstance(String processInstanceId);

    /**
     * Get process instances by business ID
     *
     * @param businessId Business ID
     * @param businessType Business type
     * @return List of process instances
     */
    List<ProcessInstance> getByBusinessId(Long businessId, String businessType);

    /**
     * Get pending approval list for user
     *
     * @param userId User ID
     * @param page Page number
     * @param pageSize Page size
     * @return List of pending approvals
     */
    List<ProcessInstance> getPendingApprovals(String userId, Integer page, Integer pageSize);

    /**
     * Get approval history/audit trail
     *
     * @param processInstanceId Process instance ID
     * @return List of approval nodes with operator, time, opinion
     */
    List<ApprovalNode> getApprovalHistory(String processInstanceId);

    /**
     * Process Start Request DTO
     */
    class StartProcessRequest {
        private String processTemplateCode;
        private String businessModule;
        private Long businessId;
        private String businessType;
        private String businessCode;
        private String businessName;
        private String initiatorId;
        private String initiatorName;
        private String changeType;
        private String changeContent;
        private String beforeData;
        private String afterData;
        private String attachments;
        private String callbackUrl;
        private Map<String, Object> variables;

        public StartProcessRequest(String processTemplateCode, String businessModule, Long businessId,
                                    String businessType, String initiatorId, String initiatorName) {
            this.processTemplateCode = processTemplateCode;
            this.businessModule = businessModule;
            this.businessId = businessId;
            this.businessType = businessType;
            this.initiatorId = initiatorId;
            this.initiatorName = initiatorName;
        }

        // Getters and setters
        public String getProcessTemplateCode() { return processTemplateCode; }
        public void setProcessTemplateCode(String processTemplateCode) { this.processTemplateCode = processTemplateCode; }
        public String getBusinessModule() { return businessModule; }
        public void setBusinessModule(String businessModule) { this.businessModule = businessModule; }
        public Long getBusinessId() { return businessId; }
        public void setBusinessId(Long businessId) { this.businessId = businessId; }
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        public String getBusinessCode() { return businessCode; }
        public void setBusinessCode(String businessCode) { this.businessCode = businessCode; }
        public String getBusinessName() { return businessName; }
        public void setBusinessName(String businessName) { this.businessName = businessName; }
        public String getInitiatorId() { return initiatorId; }
        public String getInitiatorName() { return initiatorName; }
        public String getChangeType() { return changeType; }
        public void setChangeType(String changeType) { this.changeType = changeType; }
        public String getChangeContent() { return changeContent; }
        public void setChangeContent(String changeContent) { this.changeContent = changeContent; }
        public String getBeforeData() { return beforeData; }
        public void setBeforeData(String beforeData) { this.beforeData = beforeData; }
        public String getAfterData() { return afterData; }
        public void setAfterData(String afterData) { this.afterData = afterData; }
        public String getAttachments() { return attachments; }
        public void setAttachments(String attachments) { this.attachments = attachments; }
        public String getCallbackUrl() { return callbackUrl; }
        public void setCallbackUrl(String callbackUrl) { this.callbackUrl = callbackUrl; }
        public Map<String, Object> getVariables() { return variables; }
        public void setVariables(Map<String, Object> variables) { this.variables = variables; }
    }

    /**
     * Process Instance DTO
     */
    class ProcessInstance {
        private String processInstanceId;
        private String processTemplateCode;
        private String businessModule;
        private Long businessId;
        private String businessType;
        private String businessCode;
        private String businessName;
        private String initiatorId;
        private String initiatorName;
        private String currentNode;
        private String currentApprover;
        private String status;
        private LocalDateTime submitTime;
        private LocalDateTime completeTime;
        private String rejectReason;
        private List<String> ccUsers;
        private List<String> signUsers;

        public ProcessInstance() {}

        public String getProcessInstanceId() { return processInstanceId; }
        public void setProcessInstanceId(String processInstanceId) { this.processInstanceId = processInstanceId; }
        public String getProcessTemplateCode() { return processTemplateCode; }
        public void setProcessTemplateCode(String processTemplateCode) { this.processTemplateCode = processTemplateCode; }
        public String getBusinessModule() { return businessModule; }
        public void setBusinessModule(String businessModule) { this.businessModule = businessModule; }
        public Long getBusinessId() { return businessId; }
        public void setBusinessId(Long businessId) { this.businessId = businessId; }
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        public String getBusinessCode() { return businessCode; }
        public void setBusinessCode(String businessCode) { this.businessCode = businessCode; }
        public String getBusinessName() { return businessName; }
        public void setBusinessName(String businessName) { this.businessName = businessName; }
        public String getInitiatorId() { return initiatorId; }
        public void setInitiatorId(String initiatorId) { this.initiatorId = initiatorId; }
        public String getInitiatorName() { return initiatorName; }
        public void setInitiatorName(String initiatorName) { this.initiatorName = initiatorName; }
        public String getCurrentNode() { return currentNode; }
        public void setCurrentNode(String currentNode) { this.currentNode = currentNode; }
        public String getCurrentApprover() { return currentApprover; }
        public void setCurrentApprover(String currentApprover) { this.currentApprover = currentApprover; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getSubmitTime() { return submitTime; }
        public void setSubmitTime(LocalDateTime submitTime) { this.submitTime = submitTime; }
        public LocalDateTime getCompleteTime() { return completeTime; }
        public void setCompleteTime(LocalDateTime completeTime) { this.completeTime = completeTime; }
        public String getRejectReason() { return rejectReason; }
        public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
        public List<String> getCcUsers() { return ccUsers; }
        public void setCcUsers(List<String> ccUsers) { this.ccUsers = ccUsers; }
        public List<String> getSignUsers() { return signUsers; }
        public void setSignUsers(List<String> signUsers) { this.signUsers = signUsers; }
    }

    /**
     * Approval Node DTO (audit trail entry)
     */
    class ApprovalNode {
        private String nodeId;
        private String nodeName;
        private String approverId;
        private String approverName;
        private String action;
        private String opinion;
        private LocalDateTime operateTime;
        private String ipAddress;
        private String operatorId;
        private String operatorName;

        public String getNodeId() { return nodeId; }
        public void setNodeId(String nodeId) { this.nodeId = nodeId; }
        public String getNodeName() { return nodeName; }
        public void setNodeName(String nodeName) { this.nodeName = nodeName; }
        public String getApproverId() { return approverId; }
        public void setApproverId(String approverId) { this.approverId = approverId; }
        public String getApproverName() { return approverName; }
        public void setApproverName(String approverName) { this.approverName = approverName; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getOpinion() { return opinion; }
        public void setOpinion(String opinion) { this.opinion = opinion; }
        public LocalDateTime getOperateTime() { return operateTime; }
        public void setOperateTime(LocalDateTime operateTime) { this.operateTime = operateTime; }
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public String getOperatorId() { return operatorId; }
        public void setOperatorId(String operatorId) { this.operatorId = operatorId; }
        public String getOperatorName() { return operatorName; }
        public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    }
}
