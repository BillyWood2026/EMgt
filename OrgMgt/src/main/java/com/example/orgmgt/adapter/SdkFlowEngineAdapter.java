package com.example.orgmgt.adapter;

import com.example.flowengine.sdk.api.FlowEngine;
import com.example.flowengine.sdk.model.FlowExecution;
import com.example.flowengine.sdk.model.FlowRequest;
import com.example.flowengine.sdk.model.FlowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Primary
public class SdkFlowEngineAdapter implements UnifiedProcessEngineAdapter {

    private static final Logger log = LoggerFactory.getLogger(SdkFlowEngineAdapter.class);

    private final FlowEngine flowEngine;

    public SdkFlowEngineAdapter(FlowEngine flowEngine) {
        this.flowEngine = flowEngine;
        log.info("SdkFlowEngineAdapter initialized for OrgMgt - using flow-engine-sdk");
    }

    @Override
    public ProcessInstance startProcess(StartProcessRequest request) {
        log.debug("Starting process via SDK: template={}, businessId={}",
                request.getProcessTemplateCode(), request.getBusinessId());

        FlowRequest flowRequest = new FlowRequest();
        flowRequest.setFlowId(request.getProcessTemplateCode());
        flowRequest.setBusinessKey(String.valueOf(request.getBusinessId()));
        flowRequest.setModuleCode(request.getBusinessModule());
        flowRequest.setInitiatorId(request.getInitiatorId());
        flowRequest.setInitiatorName(request.getInitiatorName());
        flowRequest.setVariables(request.getVariables());
        flowRequest.setCallbackUrl(request.getCallbackUrl());

        if (flowRequest.getVariables() == null) {
            flowRequest.setVariables(new HashMap<>());
        }
        flowRequest.getVariables().put("businessType", request.getBusinessType());
        flowRequest.getVariables().put("businessCode", request.getBusinessCode());
        flowRequest.getVariables().put("businessName", request.getBusinessName());
        flowRequest.getVariables().put("changeType", request.getChangeType());
        flowRequest.getVariables().put("changeContent", request.getChangeContent());

        FlowResponse response = flowEngine.start(flowRequest);

        ProcessInstance instance = new ProcessInstance();
        instance.setProcessInstanceId(response.getExecutionId());
        instance.setProcessTemplateCode(request.getProcessTemplateCode());
        instance.setBusinessModule(request.getBusinessModule());
        instance.setBusinessId(request.getBusinessId());
        instance.setBusinessType(request.getBusinessType());
        instance.setBusinessCode(request.getBusinessCode());
        instance.setBusinessName(request.getBusinessName());
        instance.setInitiatorId(request.getInitiatorId());
        instance.setInitiatorName(request.getInitiatorName());
        instance.setCurrentNode(response.getCurrentNodeId());
        instance.setStatus(response.getExecutionStatus());
        instance.setSubmitTime(LocalDateTime.now());
        instance.setCcUsers(new ArrayList<>());
        instance.setSignUsers(new ArrayList<>());

        log.info("Process started successfully via SDK: executionId={}", response.getExecutionId());
        return instance;
    }

    @Override
    public ProcessInstance approve(String processInstanceId, String approver, String opinion, String ipAddress) {
        Map<String, Object> variables = Map.of(
                "approver", approver, "opinion", opinion, "ipAddress", ipAddress, "action", "APPROVE"
        );
        FlowResponse response = flowEngine.signal(processInstanceId, "APPROVE", variables);
        return convertToProcessInstance(response);
    }

    @Override
    public ProcessInstance reject(String processInstanceId, String approver, String rejectReason, String ipAddress) {
        Map<String, Object> variables = Map.of(
                "approver", approver, "opinion", rejectReason, "ipAddress", ipAddress, "action", "REJECT"
        );
        FlowResponse response = flowEngine.signal(processInstanceId, "REJECT", variables);
        ProcessInstance instance = convertToProcessInstance(response);
        instance.setRejectReason(rejectReason);
        return instance;
    }

    @Override
    public ProcessInstance recall(String processInstanceId, String operator, String ipAddress) {
        FlowResponse response = flowEngine.terminate(processInstanceId, "Recalled by " + operator);
        ProcessInstance instance = convertToProcessInstance(response);
        instance.setStatus("RECALLED");
        instance.setCompleteTime(LocalDateTime.now());
        return instance;
    }

    @Override
    public ProcessInstance addSign(String processInstanceId, String operator, String additionalApprover,
                                    String opinion, String ipAddress) {
        Map<String, Object> variables = Map.of(
                "operator", operator, "additionalApprover", additionalApprover,
                "opinion", opinion, "action", "ADD_SIGN"
        );
        FlowResponse response = flowEngine.signal(processInstanceId, "ADD_SIGN", variables);
        ProcessInstance instance = convertToProcessInstance(response);
        if (instance.getSignUsers() == null) instance.setSignUsers(new ArrayList<>());
        instance.getSignUsers().add(additionalApprover);
        return instance;
    }

    @Override
    public ProcessInstance cc(String processInstanceId, String operator, List<String> ccUsers, String ipAddress) {
        Map<String, Object> variables = Map.of(
                "operator", operator, "ccUsers", String.join(",", ccUsers), "action", "CC"
        );
        flowEngine.signal(processInstanceId, "CC", variables);
        ProcessInstance instance = getProcessInstance(processInstanceId);
        if (instance.getCcUsers() == null) instance.setCcUsers(new ArrayList<>());
        instance.getCcUsers().addAll(ccUsers);
        return instance;
    }

    @Override
    public ProcessInstance terminate(String processInstanceId, String operator, String terminateReason, String ipAddress) {
        FlowResponse response = flowEngine.terminate(processInstanceId, terminateReason);
        ProcessInstance instance = convertToProcessInstance(response);
        instance.setCompleteTime(LocalDateTime.now());
        return instance;
    }

    @Override
    public ProcessInstance getProcessInstance(String processInstanceId) {
        FlowExecution execution = flowEngine.getExecution(processInstanceId);
        if (execution == null) return null;

        ProcessInstance instance = new ProcessInstance();
        instance.setProcessInstanceId(execution.getExecutionId());
        instance.setProcessTemplateCode(execution.getFlowId());
        instance.setCurrentNode(execution.getCurrentNodeId());
        instance.setStatus(execution.getStatus());
        instance.setSubmitTime(execution.getStartTime());
        instance.setCompleteTime(execution.getEndTime());
        instance.setCcUsers(new ArrayList<>());
        instance.setSignUsers(new ArrayList<>());
        return instance;
    }

    @Override
    public List<ProcessInstance> getByBusinessId(Long businessId, String businessType) {
        List<FlowExecution> executions = flowEngine.queryByBusinessKey(String.valueOf(businessId));
        List<ProcessInstance> instances = new ArrayList<>();
        for (FlowExecution execution : executions) {
            ProcessInstance instance = new ProcessInstance();
            instance.setProcessInstanceId(execution.getExecutionId());
            instance.setProcessTemplateCode(execution.getFlowId());
            instance.setBusinessId(businessId);
            instance.setBusinessType(businessType);
            instance.setCurrentNode(execution.getCurrentNodeId());
            instance.setStatus(execution.getStatus());
            instance.setSubmitTime(execution.getStartTime());
            instance.setCompleteTime(execution.getEndTime());
            instances.add(instance);
        }
        return instances;
    }

    @Override
    public List<ProcessInstance> getPendingApprovals(String userId, Integer page, Integer pageSize) {
        List<FlowResponse.PendingTask> tasks = flowEngine.getPendingTasks(userId);
        List<ProcessInstance> instances = new ArrayList<>();
        for (FlowResponse.PendingTask task : tasks) {
            ProcessInstance instance = getProcessInstance(task.getTaskId());
            if (instance != null) instances.add(instance);
        }
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, instances.size());
        return start >= instances.size() ? new ArrayList<>() : instances.subList(start, end);
    }

    @Override
    public List<ApprovalNode> getApprovalHistory(String processInstanceId) {
        List<ApprovalNode> history = new ArrayList<>();
        List<com.example.flowengine.sdk.model.ExecutionHistory> executionHistory =
                flowEngine.getExecutionHistory(processInstanceId);
        for (com.example.flowengine.sdk.model.ExecutionHistory record : executionHistory) {
            ApprovalNode node = new ApprovalNode();
            node.setNodeId(record.getNodeId());
            node.setNodeName(record.getNodeName());
            node.setApproverId(record.getOperatorId());
            node.setApproverName(record.getOperatorName());
            node.setAction(record.getActionType());
            node.setOpinion(record.getActionDescription());
            node.setOperateTime(record.getActionTime());
            node.setOperatorId(record.getOperatorId());
            node.setOperatorName(record.getOperatorName());
            history.add(node);
        }
        return history;
    }

    private ProcessInstance convertToProcessInstance(FlowResponse response) {
        ProcessInstance instance = new ProcessInstance();
        instance.setProcessInstanceId(response.getExecutionId());
        instance.setCurrentNode(response.getCurrentNodeId());
        instance.setStatus(response.getExecutionStatus());
        instance.setSubmitTime(response.getStartTime());
        instance.setCompleteTime(response.getEndTime());
        instance.setCcUsers(new ArrayList<>());
        instance.setSignUsers(new ArrayList<>());
        return instance;
    }
}
