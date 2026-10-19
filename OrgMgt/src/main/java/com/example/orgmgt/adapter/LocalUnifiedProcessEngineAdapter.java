package com.example.orgmgt.adapter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Local Unified Process Engine Adapter (Fallback Implementation)
 *
 * Provides local fallback implementation when platform unified process engine
 * is unavailable. Implements a simple three-node approval flow:
 *   DIRECT_SUPERVISOR -> HR_REVIEW -> FINAL_APPROVAL
 *
 * In production, this should be replaced by a Feign client that calls
 * the actual platform unified process engine service.
 *
 * Note: All approval nodes, approvers, and routing rules are configured
 * by the unified process engine in production, not hardcoded here.
 *
 * Activates only when FlowEngine SDK bean is not available.
 */
@Component
@ConditionalOnMissingBean(name = "sdkFlowEngineAdapter")
public class LocalUnifiedProcessEngineAdapter implements UnifiedProcessEngineAdapter {

    // Local storage for process instances (fallback only)
    private final Map<String, ProcessInstance> processInstanceStore = new ConcurrentHashMap<>();
    private final Map<String, List<ApprovalNode>> approvalHistoryStore = new ConcurrentHashMap<>();
    private final AtomicLong processIdCounter = new AtomicLong(1);

    // Default workflow nodes for local fallback
    private static final List<String> DEFAULT_WORKFLOW_NODES = Arrays.asList(
        "DIRECT_SUPERVISOR", "HR_REVIEW", "FINAL_APPROVAL"
    );

    @Override
    public ProcessInstance startProcess(StartProcessRequest request) {
        String processInstanceId = "PI-" + System.currentTimeMillis() + "-" + processIdCounter.getAndIncrement();

        ProcessInstance instance = new ProcessInstance();
        instance.setProcessInstanceId(processInstanceId);
        instance.setProcessTemplateCode(request.getProcessTemplateCode());
        instance.setBusinessModule(request.getBusinessModule());
        instance.setBusinessId(request.getBusinessId());
        instance.setBusinessType(request.getBusinessType());
        instance.setBusinessCode(request.getBusinessCode());
        instance.setBusinessName(request.getBusinessName());
        instance.setInitiatorId(request.getInitiatorId());
        instance.setInitiatorName(request.getInitiatorName());
        instance.setCurrentNode(DEFAULT_WORKFLOW_NODES.get(0));
        instance.setStatus("PENDING");
        instance.setSubmitTime(LocalDateTime.now());
        instance.setCcUsers(new ArrayList<>());
        instance.setSignUsers(new ArrayList<>());

        processInstanceStore.put(processInstanceId, instance);
        approvalHistoryStore.put(processInstanceId, new ArrayList<>());

        // Record submit node
        ApprovalNode submitNode = new ApprovalNode();
        submitNode.setNodeId("START");
        submitNode.setNodeName("Submit");
        submitNode.setOperatorId(request.getInitiatorId());
        submitNode.setOperatorName(request.getInitiatorName());
        submitNode.setAction("SUBMIT");
        submitNode.setOperateTime(LocalDateTime.now());
        approvalHistoryStore.get(processInstanceId).add(submitNode);

        return instance;
    }

    @Override
    public ProcessInstance approve(String processInstanceId, String approver, String opinion, String ipAddress) {
        ProcessInstance instance = processInstanceStore.get(processInstanceId);
        if (instance == null) {
            throw new IllegalStateException("Process instance not found: " + processInstanceId);
        }

        // Validate self-approval exclusion
        if (instance.getInitiatorId() != null && instance.getInitiatorId().equals(approver)) {
            throw new IllegalStateException(
                "Self-approval is prohibited. Initiator cannot approve their own request.");
        }

        int currentIndex = DEFAULT_WORKFLOW_NODES.indexOf(instance.getCurrentNode());

        // Record approval node
        ApprovalNode node = new ApprovalNode();
        node.setNodeId(instance.getCurrentNode());
        node.setNodeName(getNodeName(instance.getCurrentNode()));
        node.setApproverId(approver);
        node.setAction("APPROVE");
        node.setOpinion(opinion);
        node.setOperateTime(LocalDateTime.now());
        node.setIpAddress(ipAddress);
        approvalHistoryStore.get(processInstanceId).add(node);

        if (currentIndex >= DEFAULT_WORKFLOW_NODES.size() - 1) {
            // Final approval completed
            instance.setStatus("APPROVED");
            instance.setCompleteTime(LocalDateTime.now());
            instance.setCurrentNode(null);
        } else {
            // Move to next node
            instance.setCurrentNode(DEFAULT_WORKFLOW_NODES.get(currentIndex + 1));
        }

        return instance;
    }

    @Override
    public ProcessInstance reject(String processInstanceId, String approver, String rejectReason, String ipAddress) {
        ProcessInstance instance = processInstanceStore.get(processInstanceId);
        if (instance == null) {
            throw new IllegalStateException("Process instance not found: " + processInstanceId);
        }

        // Record reject node
        ApprovalNode node = new ApprovalNode();
        node.setNodeId(instance.getCurrentNode());
        node.setNodeName(getNodeName(instance.getCurrentNode()));
        node.setApproverId(approver);
        node.setAction("REJECT");
        node.setOpinion(rejectReason);
        node.setOperateTime(LocalDateTime.now());
        node.setIpAddress(ipAddress);
        approvalHistoryStore.get(processInstanceId).add(node);

        instance.setStatus("REJECTED");
        instance.setRejectReason(rejectReason);
        instance.setCompleteTime(LocalDateTime.now());
        // Return to initiator for modification (per SRS: rejection returns to initiator)
        instance.setCurrentNode("INITIATOR");

        return instance;
    }

    @Override
    public ProcessInstance recall(String processInstanceId, String operator, String ipAddress) {
        ProcessInstance instance = processInstanceStore.get(processInstanceId);
        if (instance == null) {
            throw new IllegalStateException("Process instance not found: " + processInstanceId);
        }

        // Only un-finalized instances can be recalled
        if ("APPROVED".equals(instance.getStatus()) || "REJECTED".equals(instance.getStatus())
            || "TERMINATED".equals(instance.getStatus())) {
            throw new IllegalStateException("Cannot recall finalized process instance");
        }

        // Only initiator can recall
        if (!instance.getInitiatorId().equals(operator)) {
            throw new IllegalStateException("Only initiator can recall the process");
        }

        ApprovalNode node = new ApprovalNode();
        node.setNodeId("RECALL");
        node.setNodeName("Recall");
        node.setOperatorId(operator);
        node.setAction("RECALL");
        node.setOperateTime(LocalDateTime.now());
        node.setIpAddress(ipAddress);
        approvalHistoryStore.get(processInstanceId).add(node);

        instance.setStatus("RECALLED");
        instance.setCompleteTime(LocalDateTime.now());

        return instance;
    }

    @Override
    public ProcessInstance addSign(String processInstanceId, String operator, String additionalApprover,
                                    String opinion, String ipAddress) {
        ProcessInstance instance = processInstanceStore.get(processInstanceId);
        if (instance == null) {
            throw new IllegalStateException("Process instance not found: " + processInstanceId);
        }

        if (instance.getSignUsers() == null) {
            instance.setSignUsers(new ArrayList<>());
        }
        instance.getSignUsers().add(additionalApprover);

        ApprovalNode node = new ApprovalNode();
        node.setNodeId("ADD_SIGN");
        node.setNodeName("Add Sign: " + additionalApprover);
        node.setOperatorId(operator);
        node.setAction("ADD_SIGN");
        node.setOpinion(opinion);
        node.setOperateTime(LocalDateTime.now());
        node.setIpAddress(ipAddress);
        approvalHistoryStore.get(processInstanceId).add(node);

        return instance;
    }

    @Override
    public ProcessInstance cc(String processInstanceId, String operator, List<String> ccUsers, String ipAddress) {
        ProcessInstance instance = processInstanceStore.get(processInstanceId);
        if (instance == null) {
            throw new IllegalStateException("Process instance not found: " + processInstanceId);
        }

        if (instance.getCcUsers() == null) {
            instance.setCcUsers(new ArrayList<>());
        }
        instance.getCcUsers().addAll(ccUsers);

        ApprovalNode node = new ApprovalNode();
        node.setNodeId("CC");
        node.setNodeName("CC: " + String.join(",", ccUsers));
        node.setOperatorId(operator);
        node.setAction("CC");
        node.setOperateTime(LocalDateTime.now());
        node.setIpAddress(ipAddress);
        approvalHistoryStore.get(processInstanceId).add(node);

        return instance;
    }

    @Override
    public ProcessInstance terminate(String processInstanceId, String operator, String terminateReason, String ipAddress) {
        ProcessInstance instance = processInstanceStore.get(processInstanceId);
        if (instance == null) {
            throw new IllegalStateException("Process instance not found: " + processInstanceId);
        }

        ApprovalNode node = new ApprovalNode();
        node.setNodeId("TERMINATE");
        node.setNodeName("Terminate");
        node.setOperatorId(operator);
        node.setAction("TERMINATE");
        node.setOpinion(terminateReason);
        node.setOperateTime(LocalDateTime.now());
        node.setIpAddress(ipAddress);
        approvalHistoryStore.get(processInstanceId).add(node);

        instance.setStatus("TERMINATED");
        instance.setCompleteTime(LocalDateTime.now());

        return instance;
    }

    @Override
    public ProcessInstance getProcessInstance(String processInstanceId) {
        return processInstanceStore.get(processInstanceId);
    }

    @Override
    public List<ProcessInstance> getByBusinessId(Long businessId, String businessType) {
        List<ProcessInstance> result = new ArrayList<>();
        for (ProcessInstance instance : processInstanceStore.values()) {
            if (businessId.equals(instance.getBusinessId()) && businessType.equals(instance.getBusinessType())) {
                result.add(instance);
            }
        }
        return result;
    }

    @Override
    public List<ProcessInstance> getPendingApprovals(String userId, Integer page, Integer pageSize) {
        List<ProcessInstance> result = new ArrayList<>();
        for (ProcessInstance instance : processInstanceStore.values()) {
            if ("PENDING".equals(instance.getStatus()) && userId.equals(instance.getCurrentApprover())) {
                result.add(instance);
            }
        }
        // Simple pagination
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, result.size());
        if (start >= result.size()) {
            return new ArrayList<>();
        }
        return result.subList(start, end);
    }

    @Override
    public List<ApprovalNode> getApprovalHistory(String processInstanceId) {
        return approvalHistoryStore.getOrDefault(processInstanceId, new ArrayList<>());
    }

    private String getNodeName(String nodeCode) {
        switch (nodeCode) {
            case "DIRECT_SUPERVISOR": return "Direct Supervisor Review";
            case "HR_REVIEW": return "HR Review";
            case "FINAL_APPROVAL": return "Final Approval";
            case "INITIATOR": return "Initiator";
            default: return nodeCode;
        }
    }
}
