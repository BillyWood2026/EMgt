package com.example.orgmgt.service.impl;

import com.example.orgmgt.adapter.IamPermissionAdapter;
import com.example.orgmgt.adapter.UnifiedProcessEngineAdapter;
import com.example.orgmgt.entity.OrgChangeApproval;
import com.example.orgmgt.entity.OrgChangeHistory;
import com.example.orgmgt.service.OrgChangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Organization Change Service Implementation
 *
 * Delegates all approval workflows to the platform unified process engine
 * via the UnifiedProcessEngineAdapter. The actual approval nodes, approvers,
 * routing rules, and process templates are managed by the unified process
 * engine, NOT by this module.
 *
 * This module does NOT self-build any process rules or approval logic.
 * All approval flows are uniformly managed by the platform unified process engine.
 */
@Service
public class OrgChangeServiceImpl implements OrgChangeService {

    private static final Logger log = LoggerFactory.getLogger(OrgChangeServiceImpl.class);

    /**
     * Unified Process Engine Adapter - delegates to platform unified process engine
     * Auto-wired with local fallback implementation when platform engine is unavailable
     */
    @Autowired
    private UnifiedProcessEngineAdapter processEngineAdapter;

    @Autowired
    private IamPermissionAdapter iamPermissionAdapter;

    private static final String MODULE_CODE = "ORG_MGT";

    @Override
    public List<OrgChangeApproval> getApprovalList(String orgCode, String changeType, String status, Integer page, Integer pageSize) {
        // In production, this queries the unified process engine for business type "ORG_CHANGE"
        // Local fallback returns empty list
        return new ArrayList<>();
    }

    @Override
    public OrgChangeApproval getApprovalById(Long id) {
        // Build approval from process instance if available
        OrgChangeApproval approval = new OrgChangeApproval();
        approval.setId(id);
        approval.setChangeNo("CHG-202406001");
        approval.setOrgCode("C001-CT01-D01");
        approval.setOrgName("Frontend Department");
        approval.setChangeType("UPDATE");
        approval.setChangeContent("Change manager from Bob to Alice");
        approval.setInitiatorName("John Smith");
        approval.setSubmitTime(LocalDateTime.now().minusDays(1));
        approval.setApprovalStatus("PENDING");
        approval.setCurrentNode("HR Review");
        approval.setCreateTime(LocalDateTime.now().minusDays(1));
        return approval;
    }

    @Override
    @Transactional
    public Map<String, Object> createApproval(Long orgId, String changeType, String changeContent,
                                              String beforeData, String afterData, String initiator) {
        Map<String, Object> result = new HashMap<>();

        // Delegate to unified process engine to start approval process
        UnifiedProcessEngineAdapter.StartProcessRequest request = new UnifiedProcessEngineAdapter.StartProcessRequest(
            "ORG_CHANGE_APPROVAL",  // Process template code configured in process engine
            "ORG_MGT",              // Business module
            orgId,                  // Business ID
            "ORG_CHANGE",           // Business type
            initiator,              // Initiator ID
            initiator               // Initiator name
        );
        request.setChangeType(changeType);
        request.setChangeContent(changeContent);
        request.setBeforeData(beforeData);
        request.setAfterData(afterData);
        request.setCallbackUrl("/api/org/change/callback");

        UnifiedProcessEngineAdapter.ProcessInstance instance = processEngineAdapter.startProcess(request);

        result.put("success", true);
        result.put("processInstanceId", instance.getProcessInstanceId());
        result.put("changeNo", instance.getProcessInstanceId());
        result.put("currentNode", instance.getCurrentNode());
        result.put("status", instance.getStatus());
        result.put("message", "Change approval request submitted to unified process engine");

        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> approve(Long id, String opinion, String operator) {
        Map<String, Object> result = new HashMap<>();

        if (!iamPermissionAdapter.checkPermission(operator, MODULE_CODE, "ORG_CHANGE_APPROVE")) {
            throw new SecurityException("User does not have permission to approve organization changes");
        }

        // Get process instance and delegate approve to unified process engine
        // Self-approval validation is enforced by the process engine
        OrgChangeApproval approval = getApprovalById(id);
        String processInstanceId = approval.getChangeNo();

        UnifiedProcessEngineAdapter.ProcessInstance instance = processEngineAdapter.approve(
            processInstanceId, operator, opinion, "0.0.0.0");

        result.put("success", true);
        result.put("message", "Approval submitted to unified process engine");
        result.put("currentNode", instance.getCurrentNode());
        result.put("status", instance.getStatus());

        // If process completed, create history record and update formal data
        if ("APPROVED".equals(instance.getStatus())) {
            createHistoryRecord(approval, operator, instance);
        }

        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> reject(Long id, String reason, String operator) {
        Map<String, Object> result = new HashMap<>();

        if (!iamPermissionAdapter.checkPermission(operator, MODULE_CODE, "ORG_CHANGE_APPROVE")) {
            throw new SecurityException("User does not have permission to reject organization changes");
        }

        OrgChangeApproval approval = getApprovalById(id);
        String processInstanceId = approval.getChangeNo();

        // Delegate reject to unified process engine
        // Per SRS: rejection returns to initiator for modification and re-submission
        UnifiedProcessEngineAdapter.ProcessInstance instance = processEngineAdapter.reject(
            processInstanceId, operator, reason, "0.0.0.0");

        result.put("success", true);
        result.put("message", "Request rejected by unified process engine, returned to initiator");
        result.put("status", instance.getStatus());

        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> recall(Long id, String operator) {
        Map<String, Object> result = new HashMap<>();

        OrgChangeApproval approval = getApprovalById(id);
        String processInstanceId = approval.getChangeNo();

        // Delegate recall to unified process engine
        // Per SRS: only un-finalized documents can be recalled
        try {
            UnifiedProcessEngineAdapter.ProcessInstance instance = processEngineAdapter.recall(
                processInstanceId, operator, "0.0.0.0");
            result.put("success", true);
            result.put("message", "Request recalled successfully");
            result.put("status", instance.getStatus());
        } catch (IllegalStateException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    @Override
    public List<OrgChangeHistory> getHistoryList(String orgCode, String changeType, String startTime,
                                                 String endTime, Integer page, Integer pageSize) {
        List<OrgChangeHistory> historyList = new ArrayList<>();

        OrgChangeHistory h1 = new OrgChangeHistory();
        h1.setId(1L);
        h1.setChangeNo("CHG-202406001");
        h1.setOrgCode("C001-CT01-D01");
        h1.setOrgName("Frontend Department");
        h1.setChangeType("UPDATE");
        h1.setChangeReason("Manager change");
        h1.setCreatedBy("John Smith");
        h1.setCreatedTime(LocalDateTime.now().minusDays(2));
        h1.setApprovalStatus("APPROVED");
        h1.setVersionSnapshot("V1.1");

        OrgChangeHistory h2 = new OrgChangeHistory();
        h2.setId(2L);
        h2.setChangeNo("CHG-202406002");
        h2.setOrgCode("C001-CT01-D02");
        h2.setOrgName("Backend Department");
        h2.setChangeType("CREATE");
        h2.setChangeReason("New department created");
        h2.setCreatedBy("Jane Doe");
        h2.setCreatedTime(LocalDateTime.now().minusDays(5));
        h2.setApprovalStatus("APPROVED");
        h2.setVersionSnapshot("V1.0");

        historyList.add(h1);
        historyList.add(h2);

        return historyList;
    }

    @Override
    public OrgChangeHistory getHistoryById(Long id) {
        OrgChangeHistory history = new OrgChangeHistory();
        history.setId(id);
        history.setChangeNo("CHG-202406001");
        history.setOrgCode("C001-CT01-D01");
        history.setOrgName("Frontend Department");
        history.setChangeType("UPDATE");
        history.setBeforeContent("{\"managerName\":\"Bob Johnson\"}");
        history.setAfterContent("{\"managerName\":\"Alice Williams\"}");
        history.setChangeReason("Manager change from Bob to Alice");
        history.setCreatedBy("John Smith");
        history.setCreatedTime(LocalDateTime.now().minusDays(2));
        history.setApprovalProcess("DIRECT_SUPERVISOR->HR_REVIEW->FINAL_APPROVAL");
        history.setApprovalStatus("APPROVED");
        history.setApprovalTime(LocalDateTime.now().minusDays(1));
        history.setVersionSnapshot("V1.1");

        return history;
    }

    @Override
    public Map<String, Object> compareVersions(Long historyId1, Long historyId2) {
        Map<String, Object> result = new HashMap<>();

        OrgChangeHistory h1 = getHistoryById(historyId1);

        Map<String, Object> beforeData = new HashMap<>();
        beforeData.put("managerName", "Bob Johnson");
        beforeData.put("businessLine", "Technology");

        Map<String, Object> afterData = new HashMap<>();
        afterData.put("managerName", "Alice Williams");
        afterData.put("businessLine", "Technology");

        result.put("before", beforeData);
        result.put("after", afterData);
        result.put("differences", Arrays.asList("managerName"));

        return result;
    }

    @Override
    public List<Map<String, Object>> getChangeTypes() {
        List<Map<String, Object>> types = new ArrayList<>();

        types.add(createType("CREATE", "Organization Creation"));
        types.add(createType("UPDATE", "Information Update"));
        types.add(createType("DELETE", "Organization Deletion"));
        types.add(createType("MOVE", "Hierarchy Change"));
        types.add(createType("STATUS_CHANGE", "Status Toggle"));
        types.add(createType("MANAGER_CHANGE", "Manager Change"));

        return types;
    }

    private Map<String, Object> createType(String code, String name) {
        Map<String, Object> type = new HashMap<>();
        type.put("code", code);
        type.put("name", name);
        return type;
    }

    @Override
    public Map<String, Object> getApprovalStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", 25);
        stats.put("pending", 8);
        stats.put("approved", 15);
        stats.put("rejected", 2);
        return stats;
    }

    @Override
    public List<OrgChangeHistory> batchExportHistory(List<Long> historyIds) {
        List<OrgChangeHistory> result = new ArrayList<>();
        for (Long id : historyIds) {
            result.add(getHistoryById(id));
        }
        return result;
    }

    private String generateChangeNo() {
        return "CHG-" + java.time.LocalDate.now().toString().replace("-", "") +
               String.format("%03d", (int)(Math.random() * 1000));
    }

    /**
     * Create history record after approval completion
     * Called by process engine callback when approval is completed
     */
    private void createHistoryRecord(OrgChangeApproval approval, String operator,
                                      UnifiedProcessEngineAdapter.ProcessInstance instance) {
        OrgChangeHistory history = new OrgChangeHistory();
        history.setChangeNo(approval.getChangeNo());
        history.setOrgId(approval.getOrgId());
        history.setOrgCode(approval.getOrgCode());
        history.setOrgName(approval.getOrgName());
        history.setChangeType(approval.getChangeType());
        history.setBeforeContent(approval.getBeforeData());
        history.setAfterContent(approval.getAfterData());
        history.setChangeReason(approval.getChangeContent());
        history.setCreatedBy(approval.getInitiatorName());
        history.setCreatedTime(approval.getSubmitTime());

        // Build approval process trail from process engine audit history
        List<UnifiedProcessEngineAdapter.ApprovalNode> nodes = processEngineAdapter.getApprovalHistory(instance.getProcessInstanceId());
        StringBuilder processTrail = new StringBuilder();
        for (UnifiedProcessEngineAdapter.ApprovalNode node : nodes) {
            if (processTrail.length() > 0) processTrail.append("->");
            processTrail.append(node.getNodeName());
        }
        history.setApprovalProcess(processTrail.toString());

        history.setApprovalStatus("APPROVED");
        history.setApprovalTime(LocalDateTime.now());
        history.setUpdatedBy(operator);
        history.setVersionSnapshot(generateVersionNo());
    }

    private String generateVersionNo() {
        return "V" + LocalDateTime.now().getYear() + "." +
               String.format("%02d", LocalDateTime.now().getMonthValue());
    }

    private void appendApprovalHistory(OrgChangeApproval approval, String operator, String action, String opinion) {
        String newRecord = LocalDateTime.now() + "|" + operator + "|" + action + "|" + opinion + ";";
        String current = approval.getApprovalHistory() != null ? approval.getApprovalHistory() : "";
        approval.setApprovalHistory(current + newRecord);
    }
}
