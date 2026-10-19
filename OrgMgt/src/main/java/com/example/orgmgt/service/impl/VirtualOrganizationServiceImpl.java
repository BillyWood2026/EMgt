package com.example.orgmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.orgmgt.adapter.IamPermissionAdapter;
import com.example.orgmgt.adapter.UnifiedProcessEngineAdapter;
import com.example.orgmgt.entity.VirtualOrganization;
import com.example.orgmgt.entity.VirtualOrgMember;
import com.example.orgmgt.entity.VirtualOrgHistory;
import com.example.orgmgt.mapper.VirtualOrganizationMapper;
import com.example.orgmgt.mapper.VirtualOrganizationMemberMapper;
import com.example.orgmgt.mapper.VirtualOrgHistoryMapper;
import com.example.orgmgt.service.VirtualOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Virtual Organization Service Implementation
 *
 * Manages lifecycle of cross-department virtual organizations.
 * All persistence targets virtual_organization / virtual_organization_member /
 * virtual_org_history tables. Approval workflows are delegated to the local
 * unified process engine adapter.
 *
 * Per SRS 7: Virtual organizations do not occupy entity org hierarchy,
 * do not affect formal staffing, and do not change personnel affiliation.
 */
@Service
public class VirtualOrganizationServiceImpl implements VirtualOrganizationService {

    @Autowired
    private VirtualOrganizationMapper virtualOrganizationMapper;

    @Autowired
    private VirtualOrganizationMemberMapper virtualOrgMemberMapper;

    @Autowired
    private VirtualOrgHistoryMapper virtualOrgHistoryMapper;

    @Autowired
    private UnifiedProcessEngineAdapter processEngineAdapter;

    @Autowired
    private IamPermissionAdapter iamPermissionAdapter;

    private static final String MODULE_CODE = "VIRTUAL_ORG";
    private static final String TEMPLATE_VORG_CREATE = "VIRTUAL_ORG_CREATE";
    private static final String TEMPLATE_VORG_EXTEND = "VIRTUAL_ORG_EXTEND";
    private static final String TEMPLATE_VORG_DISBAND = "VIRTUAL_ORG_DISBAND";

    @Override
    public List<Map<String, Object>> getVirtualOrgTree() {
        List<Map<String, Object>> tree = new ArrayList<>();
        tree.add(buildTypeNode("PROJECT", "Project Teams"));
        tree.add(buildTypeNode("SPECIAL", "Special Task Groups"));
        tree.add(buildTypeNode("TEMPORARY", "Temporary Teams"));
        return tree;
    }

    private Map<String, Object> buildTypeNode(String type, String typeName) {
        Map<String, Object> node = new HashMap<>();
        node.put("type", type);
        node.put("typeName", typeName);
        node.put("children", virtualOrganizationMapper.selectList(null, type, null, 0, 1000));
        return node;
    }

    @Override
    public List<VirtualOrganization> getVirtualOrgList(String keyword, String orgType, String status, Integer page, Integer pageSize) {
        int size = (pageSize == null || pageSize <= 0) ? 20 : pageSize;
        int no = (page == null || page <= 0) ? 1 : page;
        int offset = (no - 1) * size;
        String kw = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        String t = (orgType == null || orgType.trim().isEmpty()) ? null : orgType.trim();
        String s = (status == null || status.trim().isEmpty()) ? null : status.trim();
        return virtualOrganizationMapper.selectList(kw, t, s, offset, size);
    }

    @Override
    public VirtualOrganization getVirtualOrgById(Long id) {
        if (id == null) {
            return null;
        }
        return virtualOrganizationMapper.selectById(id);
    }

    @Override
    public VirtualOrganization getVirtualOrgByCode(String virtualOrgCode) {
        if (virtualOrgCode == null || virtualOrgCode.trim().isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<VirtualOrganization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VirtualOrganization::getVirtualOrgCode, virtualOrgCode).last("LIMIT 1");
        List<VirtualOrganization> list = virtualOrganizationMapper.selectList(wrapper);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    @Transactional
    public Map<String, Object> createVirtualOrg(VirtualOrganization virtualOrg, List<VirtualOrgMember> members, String initiator) {
        Map<String, Object> result = new HashMap<>();
        if (virtualOrg == null) {
            result.put("success", false);
            result.put("message", "Virtual organization payload is required");
            return result;
        }
        String op = (initiator == null || initiator.trim().isEmpty()) ? "admin" : initiator;

        if (virtualOrg.getVirtualOrgCode() == null || virtualOrg.getVirtualOrgCode().trim().isEmpty()) {
            virtualOrg.setVirtualOrgCode(generateVirtualOrgCode(virtualOrg.getOrgType()));
        }
        if (virtualOrg.getOrgStatus() == null || virtualOrg.getOrgStatus().trim().isEmpty()) {
            virtualOrg.setOrgStatus(VirtualOrganization.STATUS_PREPARING);
        }
        LocalDateTime now = LocalDateTime.now();
        virtualOrg.setCreateTime(now);
        virtualOrg.setCreatedBy(op);
        virtualOrg.setDeleted(0);
        virtualOrg.setVersion(1);
        int memberCount = (members == null) ? 0 : members.size();
        virtualOrg.setMemberCount(memberCount);

        virtualOrganizationMapper.insert(virtualOrg);

        if (members != null) {
            for (VirtualOrgMember member : members) {
                prepareMember(virtualOrg, member, op, now);
                virtualOrgMemberMapper.insert(member);
            }
        }

        insertHistory(virtualOrg, VirtualOrgHistory.CHANGE_TYPE_CREATE,
                "创建虚拟组织：" + virtualOrg.getVirtualOrgName(), op, null, null,
                VirtualOrgHistory.RESULT_APPROVED);

        try {
            initiateCreateApproval(virtualOrg, members, op);
        } catch (Exception ignored) {
            // local adapter: workflow is best-effort
        }

        result.put("success", true);
        result.put("message", "Virtual organization created successfully");
        result.put("virtualOrgId", virtualOrg.getId());
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> updateVirtualOrg(Long id, VirtualOrganization virtualOrg, String operator) {
        Map<String, Object> result = new HashMap<>();
        VirtualOrganization existing = virtualOrganizationMapper.selectById(id);
        if (existing == null) {
            result.put("success", false);
            result.put("message", "Virtual organization not found");
            return result;
        }
        String op = (operator == null || operator.trim().isEmpty()) ? "admin" : operator;

        if (virtualOrg.getVirtualOrgName() != null) {
            existing.setVirtualOrgName(virtualOrg.getVirtualOrgName());
        }
        if (virtualOrg.getOrgType() != null) {
            existing.setOrgType(virtualOrg.getOrgType());
        }
        if (virtualOrg.getLeaderId() != null) {
            existing.setLeaderId(virtualOrg.getLeaderId());
        }
        if (virtualOrg.getLeaderName() != null) {
            existing.setLeaderName(virtualOrg.getLeaderName());
        }
        if (virtualOrg.getLeadOrgId() != null) {
            existing.setLeadOrgId(virtualOrg.getLeadOrgId());
        }
        if (virtualOrg.getLeadOrgCode() != null) {
            existing.setLeadOrgCode(virtualOrg.getLeadOrgCode());
        }
        if (virtualOrg.getLeadOrgName() != null) {
            existing.setLeadOrgName(virtualOrg.getLeadOrgName());
        }
        if (virtualOrg.getParticipatingOrgs() != null) {
            existing.setParticipatingOrgs(virtualOrg.getParticipatingOrgs());
        }
        if (virtualOrg.getEffectiveDate() != null) {
            existing.setEffectiveDate(virtualOrg.getEffectiveDate());
        }
        if (virtualOrg.getExpireDate() != null) {
            existing.setExpireDate(virtualOrg.getExpireDate());
        }
        if (virtualOrg.getOrgStatus() != null) {
            existing.setOrgStatus(virtualOrg.getOrgStatus());
        }
        if (virtualOrg.getWorkObjective() != null) {
            existing.setWorkObjective(virtualOrg.getWorkObjective());
        }
        if (virtualOrg.getRemarks() != null) {
            existing.setRemarks(virtualOrg.getRemarks());
        }
        existing.setUpdatedBy(op);
        existing.setUpdateTime(LocalDateTime.now());
        virtualOrganizationMapper.update(existing);

        insertHistory(existing, "UPDATE", "更新虚拟组织信息：" + existing.getVirtualOrgName(), op, null, null,
                VirtualOrgHistory.RESULT_APPROVED);

        result.put("success", true);
        result.put("message", "Virtual organization updated successfully");
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> addMember(Long virtualOrgId, VirtualOrgMember member, String operator) {
        Map<String, Object> result = new HashMap<>();
        VirtualOrganization org = virtualOrganizationMapper.selectById(virtualOrgId);
        if (org == null) {
            result.put("success", false);
            result.put("message", "Virtual organization not found");
            return result;
        }
        if (VirtualOrganization.STATUS_DISBAND.equals(org.getOrgStatus())) {
            result.put("success", false);
            result.put("message", "Cannot add member to disbanded organization");
            return result;
        }
        if (member == null || member.getEmployeeId() == null || member.getEmployeeId().trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "employeeId is required");
            return result;
        }
        String op = (operator == null || operator.trim().isEmpty()) ? "admin" : operator;
        prepareMember(org, member, op, LocalDateTime.now());
        virtualOrgMemberMapper.insert(member);
        int count = refreshMemberCount(org);

        insertHistory(org, VirtualOrgHistory.CHANGE_TYPE_MEMBER_CHANGE,
                "添加成员：" + member.getEmployeeId() + " " + safe(member.getEmployeeName()),
                op, null, null, VirtualOrgHistory.RESULT_APPROVED);

        result.put("success", true);
        result.put("message", "Member added successfully");
        result.put("memberCount", count);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> removeMember(Long virtualOrgId, String employeeId, String reason, String operator) {
        Map<String, Object> result = new HashMap<>();
        VirtualOrganization org = virtualOrganizationMapper.selectById(virtualOrgId);
        if (org == null) {
            result.put("success", false);
            result.put("message", "Virtual organization not found");
            return result;
        }
        LambdaQueryWrapper<VirtualOrgMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VirtualOrgMember::getVirtualOrgId, virtualOrgId)
                .eq(VirtualOrgMember::getEmployeeId, employeeId)
                .eq(VirtualOrgMember::getMemberStatus, VirtualOrgMember.STATUS_ACTIVE)
                .last("LIMIT 1");
        List<VirtualOrgMember> found = virtualOrgMemberMapper.selectList(wrapper);
        if (found.isEmpty()) {
            result.put("success", false);
            result.put("message", "Active member not found: " + employeeId);
            return result;
        }
        String op = (operator == null || operator.trim().isEmpty()) ? "admin" : operator;
        VirtualOrgMember member = found.get(0);
        member.setMemberStatus(VirtualOrgMember.STATUS_LEFT);
        member.setLeaveDate(LocalDateTime.now());
        if (reason != null && !reason.trim().isEmpty()) {
            member.setRemarks(reason);
        }
        virtualOrgMemberMapper.update(member);
        int count = refreshMemberCount(org);

        insertHistory(org, VirtualOrgHistory.CHANGE_TYPE_MEMBER_CHANGE,
                "移除成员：" + employeeId + (reason == null ? "" : "，原因：" + reason),
                op, null, null, VirtualOrgHistory.RESULT_APPROVED);

        result.put("success", true);
        result.put("message", "Member removed successfully");
        result.put("memberCount", count);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> changeLeader(Long virtualOrgId, String newLeaderId, String newLeaderName,
                                            String reason, String operator) {
        Map<String, Object> result = new HashMap<>();
        VirtualOrganization org = virtualOrganizationMapper.selectById(virtualOrgId);
        if (org == null) {
            result.put("success", false);
            result.put("message", "Virtual organization not found");
            return result;
        }
        String op = (operator == null || operator.trim().isEmpty()) ? "admin" : operator;
        String oldLeader = org.getLeaderName();
        org.setLeaderId(newLeaderId);
        org.setLeaderName(newLeaderName);
        org.setUpdatedBy(op);
        org.setUpdateTime(LocalDateTime.now());
        virtualOrganizationMapper.update(org);

        insertHistory(org, VirtualOrgHistory.CHANGE_TYPE_LEADER_CHANGE,
                "负责人变更：" + safe(oldLeader) + " -> " + safe(newLeaderName)
                        + (reason == null ? "" : "，原因：" + reason),
                op, null, null, VirtualOrgHistory.RESULT_APPROVED);

        result.put("success", true);
        result.put("message", "Leader changed successfully");
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> extendExpiration(Long virtualOrgId, LocalDateTime newExpireDate,
                                                String reason, String operator) {
        Map<String, Object> result = new HashMap<>();
        VirtualOrganization org = virtualOrganizationMapper.selectById(virtualOrgId);
        if (org == null) {
            result.put("success", false);
            result.put("message", "Virtual organization not found");
            return result;
        }
        if (newExpireDate == null) {
            result.put("success", false);
            result.put("message", "newExpireDate is required");
            return result;
        }
        String op = (operator == null || operator.trim().isEmpty()) ? "admin" : operator;
        LocalDateTime oldExpireDate = org.getExpireDate();
        org.setExpireDate(newExpireDate);
        if (VirtualOrganization.STATUS_PREPARING.equals(org.getOrgStatus())
                || VirtualOrganization.STATUS_EXPIRING.equals(org.getOrgStatus())) {
            org.setOrgStatus(VirtualOrganization.STATUS_ACTIVE);
        }
        org.setUpdatedBy(op);
        org.setUpdateTime(LocalDateTime.now());
        virtualOrganizationMapper.update(org);

        insertHistory(org, VirtualOrgHistory.CHANGE_TYPE_EXTEND,
                "延期：" + (oldExpireDate == null ? "-" : oldExpireDate) + " -> " + newExpireDate
                        + (reason == null ? "" : "，原因：" + reason),
                op, null, null, VirtualOrgHistory.RESULT_APPROVED);

        try {
            initiateExtendApproval(virtualOrgId, oldExpireDate, newExpireDate, reason, op);
        } catch (Exception ignored) {
            // best-effort workflow
        }

        result.put("success", true);
        result.put("message", "Expiration extended successfully");
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> disbandVirtualOrg(Long id, String reason, String workSummaryPath, String operator) {
        Map<String, Object> result = new HashMap<>();
        VirtualOrganization org = virtualOrganizationMapper.selectById(id);
        if (org == null) {
            result.put("success", false);
            result.put("message", "Virtual organization not found");
            return result;
        }
        if (VirtualOrganization.STATUS_DISBAND.equals(org.getOrgStatus())) {
            result.put("success", false);
            result.put("message", "Virtual organization is already disbanded");
            return result;
        }
        String op = (operator == null || operator.trim().isEmpty()) ? "admin" : operator;

        String changeNo = generateChangeNo("DISBAND");
        String beforeData = serializeVirtualOrg(org);
        Map<String, Object> afterMap = new HashMap<>();
        afterMap.put("orgStatus", VirtualOrganization.STATUS_DISBAND);
        afterMap.put("expireDate", LocalDateTime.now());
        afterMap.put("disbandReason", reason);

        VirtualOrgHistory history = new VirtualOrgHistory();
        history.setChangeNo(changeNo);
        history.setVirtualOrgId(id);
        history.setVirtualOrgCode(org.getVirtualOrgCode());
        history.setVirtualOrgName(org.getVirtualOrgName());
        history.setChangeType(VirtualOrgHistory.CHANGE_TYPE_DISBAND);
        history.setBeforeData(beforeData);
        history.setAfterData(afterMap.toString());
        history.setChangeDesc("虚拟组织解散，原因：" + safe(reason));
        history.setInitiatorId(op);
        history.setInitiatorName(op);
        history.setChangeTime(LocalDateTime.now());
        history.setApprovalNodes("WORK_VERIFY->DEPT_REVIEW->HR_ARCHIVE");
        history.setApprovalResult(VirtualOrgHistory.RESULT_APPROVED);
        history.setAttachmentPath(workSummaryPath);
        history.setRemarks("全生命周期归档，永久留存");
        history.setCreateTime(LocalDateTime.now());
        virtualOrgHistoryMapper.insert(history);

        org.setOrgStatus(VirtualOrganization.STATUS_DISBAND);
        org.setExpireDate(LocalDateTime.now());
        org.setUpdatedBy(op);
        org.setUpdateTime(LocalDateTime.now());
        virtualOrganizationMapper.update(org);

        try {
            initiateDisbandApproval(id, changeNo, reason, workSummaryPath, op);
        } catch (Exception ignored) {
            // best-effort workflow
        }

        result.put("success", true);
        result.put("message", "Virtual organization disbanded");
        result.put("changeNo", changeNo);
        result.put("historyId", history.getId());
        return result;
    }

    @Override
    public List<VirtualOrgMember> getMembers(Long virtualOrgId) {
        if (virtualOrgId == null) {
            return new ArrayList<>();
        }
        return virtualOrgMemberMapper.selectByVirtualOrgId(virtualOrgId);
    }

    @Override
    public List<VirtualOrganization> getExpiringOrgs(int daysThreshold) {
        return virtualOrganizationMapper.selectExpiringOrgs(daysThreshold);
    }

    @Override
    public List<VirtualOrgHistory> getHistory(String virtualOrgCode, String changeType, Integer page, Integer pageSize) {
        String code = (virtualOrgCode == null || virtualOrgCode.trim().isEmpty()) ? null : virtualOrgCode.trim();
        String type = (changeType == null || changeType.trim().isEmpty()) ? null : changeType.trim();
        return virtualOrgHistoryMapper.selectList(code, type, null);
    }

    @Override
    public VirtualOrgHistory getHistoryById(Long id) {
        return virtualOrgHistoryMapper.selectById(id);
    }

    @Override
    public List<VirtualOrganization> getVirtualOrgsByEmployee(String employeeId) {
        List<VirtualOrganization> orgs = new ArrayList<>();
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return orgs;
        }
        List<Long> orgIds = virtualOrgMemberMapper.selectOrgIdsByEmployee(employeeId);
        for (Long orgId : orgIds) {
            VirtualOrganization org = virtualOrganizationMapper.selectById(orgId);
            if (org != null) {
                orgs.add(org);
            }
        }
        return orgs;
    }

    @Override
    public Map<String, Object> getVirtualOrgStats() {
        Map<String, Object> stats = new HashMap<>();
        List<VirtualOrganization> all = virtualOrganizationMapper.selectAll();
        stats.put("total", all.size());
        stats.put("active", safeCount(virtualOrganizationMapper.countByStatus(VirtualOrganization.STATUS_ACTIVE)));
        stats.put("preparing", safeCount(virtualOrganizationMapper.countByStatus(VirtualOrganization.STATUS_PREPARING)));
        stats.put("expiring", safeCount(virtualOrganizationMapper.countByStatus(VirtualOrganization.STATUS_EXPIRING)));
        stats.put("disbanded", safeCount(virtualOrganizationMapper.countByStatus(VirtualOrganization.STATUS_DISBAND)));
        stats.put("project", virtualOrganizationMapper.selectList(null, VirtualOrganization.TYPE_PROJECT, null, 0, 1000).size());
        stats.put("special", virtualOrganizationMapper.selectList(null, VirtualOrganization.TYPE_SPECIAL, null, 0, 1000).size());
        stats.put("temporary", virtualOrganizationMapper.selectList(null, VirtualOrganization.TYPE_TEMPORARY, null, 0, 1000).size());
        return stats;
    }

    @Override
    @Transactional
    public Map<String, Object> batchImportMembers(Long virtualOrgId, List<Map<String, Object>> memberList, String operator) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        VirtualOrganization org = virtualOrganizationMapper.selectById(virtualOrgId);
        if (org == null) {
            result.put("success", false);
            result.put("message", "Virtual organization not found");
            result.put("successCount", 0);
            result.put("failCount", memberList == null ? 0 : memberList.size());
            result.put("errors", errors);
            return result;
        }

        if (memberList != null) {
            for (Map<String, Object> memberData : memberList) {
                VirtualOrgMember member = new VirtualOrgMember();
                member.setEmployeeId(asString(memberData.get("employeeId")));
                member.setEmployeeName(asString(memberData.get("employeeName")));
                member.setVirtualRole(asString(memberData.get("virtualRole")));
                Map<String, Object> addResult = addMember(virtualOrgId, member, operator);
                if (Boolean.TRUE.equals(addResult.get("success"))) {
                    successCount++;
                } else {
                    failCount++;
                    errors.add(asString(addResult.get("message")));
                }
            }
        }

        result.put("success", true);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        return result;
    }

    @Override
    public List<VirtualOrganization> batchExport(List<Long> virtualOrgIds) {
        List<VirtualOrganization> result = new ArrayList<>();
        if (virtualOrgIds == null) {
            return result;
        }
        for (Long id : virtualOrgIds) {
            VirtualOrganization org = virtualOrganizationMapper.selectById(id);
            if (org != null) {
                result.add(org);
            }
        }
        return result;
    }

    @Override
    public String generateVirtualOrgCode(String orgType) {
        String prefix = (orgType == null || orgType.trim().isEmpty())
                ? "X" : orgType.substring(0, 1).toUpperCase();
        return "VORG-" + prefix + "-" + System.currentTimeMillis()
                + String.format("%03d", (int) (Math.random() * 1000));
    }

    // ---------- internal helpers ----------

    private void prepareMember(VirtualOrganization org, VirtualOrgMember member, String operator, LocalDateTime now) {
        member.setVirtualOrgId(org.getId());
        member.setVirtualOrgCode(org.getVirtualOrgCode());
        member.setVirtualOrgName(org.getVirtualOrgName());
        if (member.getJoinDate() == null) {
            member.setJoinDate(now);
        }
        if (member.getMemberStatus() == null || member.getMemberStatus().trim().isEmpty()) {
            member.setMemberStatus(VirtualOrgMember.STATUS_ACTIVE);
        }
        member.setCreateTime(now);
        member.setCreatedBy(operator);
        member.setDeleted(0);
        member.setVersion(1);
    }

    private int refreshMemberCount(VirtualOrganization org) {
        Integer count = virtualOrgMemberMapper.countByVirtualOrgId(org.getId());
        int c = count == null ? 0 : count;
        virtualOrganizationMapper.updateMemberCount(org.getId(), c);
        return c;
    }

    private void insertHistory(VirtualOrganization org, String changeType, String desc, String operator,
                               String beforeData, String afterData, String approvalResult) {
        try {
            VirtualOrgHistory history = new VirtualOrgHistory();
            history.setChangeNo(generateChangeNo(changeType));
            history.setVirtualOrgId(org.getId());
            history.setVirtualOrgCode(org.getVirtualOrgCode());
            history.setVirtualOrgName(org.getVirtualOrgName());
            history.setChangeType(changeType);
            history.setBeforeData(beforeData);
            history.setAfterData(afterData);
            history.setChangeDesc(desc);
            history.setInitiatorId(operator);
            history.setInitiatorName(operator);
            history.setChangeTime(LocalDateTime.now());
            history.setApprovalResult(approvalResult);
            history.setCreateTime(LocalDateTime.now());
            virtualOrgHistoryMapper.insert(history);
        } catch (Exception ignored) {
            // history must not break the main transaction path
        }
    }

    private int safeCount(Integer c) {
        return c == null ? 0 : c;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String asString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private String serializeVirtualOrg(VirtualOrganization org) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", org.getId());
        data.put("virtualOrgCode", org.getVirtualOrgCode());
        data.put("virtualOrgName", org.getVirtualOrgName());
        data.put("orgType", org.getOrgType());
        data.put("leaderId", org.getLeaderId());
        data.put("leaderName", org.getLeaderName());
        data.put("leadOrgId", org.getLeadOrgId());
        data.put("leadOrgCode", org.getLeadOrgCode());
        data.put("leadOrgName", org.getLeadOrgName());
        data.put("participatingOrgs", org.getParticipatingOrgs());
        data.put("effectiveDate", org.getEffectiveDate());
        data.put("expireDate", org.getExpireDate());
        data.put("orgStatus", org.getOrgStatus());
        data.put("memberCount", org.getMemberCount());
        data.put("workObjective", org.getWorkObjective());
        data.put("remarks", org.getRemarks());
        data.put("createdBy", org.getCreatedBy());
        data.put("createTime", org.getCreateTime());
        return data.toString();
    }

    private String generateChangeNo(String changeType) {
        String prefix = changeType == null ? "X" : changeType.substring(0, 1);
        return "VCHG-" + prefix + "-" + System.currentTimeMillis();
    }

    private void initiateCreateApproval(VirtualOrganization org, List<VirtualOrgMember> members, String initiator) {
        UnifiedProcessEngineAdapter.StartProcessRequest request = new UnifiedProcessEngineAdapter.StartProcessRequest(
            TEMPLATE_VORG_CREATE,
            MODULE_CODE,
            org.getId(),
            "VORG_CREATE",
            initiator,
            initiator
        );
        request.setBusinessCode(org.getVirtualOrgCode());
        request.setBusinessName(org.getVirtualOrgName());
        request.setChangeType("CREATE");
        request.setChangeContent("Create virtual organization: " + org.getVirtualOrgName());
        request.setCallbackUrl("/api/virtual-org/callback");

        Map<String, Object> variables = new HashMap<>();
        variables.put("orgType", org.getOrgType());
        variables.put("leadOrgId", org.getLeadOrgId());
        variables.put("participatingOrgs", org.getParticipatingOrgs());
        variables.put("memberCount", members != null ? members.size() : 0);
        request.setVariables(variables);

        processEngineAdapter.startProcess(request);
    }

    private void initiateExtendApproval(Long virtualOrgId, LocalDateTime oldDate, LocalDateTime newDate, String reason, String operator) {
        VirtualOrganization org = virtualOrganizationMapper.selectById(virtualOrgId);
        if (org == null) {
            return;
        }
        UnifiedProcessEngineAdapter.StartProcessRequest request = new UnifiedProcessEngineAdapter.StartProcessRequest(
            TEMPLATE_VORG_EXTEND,
            MODULE_CODE,
            virtualOrgId,
            "VORG_EXTEND",
            operator,
            operator
        );
        request.setBusinessCode(org.getVirtualOrgCode());
        request.setBusinessName(org.getVirtualOrgName());
        request.setChangeType("EXTEND");
        request.setChangeContent("Extend virtual org from " + oldDate + " to " + newDate);
        request.setCallbackUrl("/api/virtual-org/callback");

        Map<String, Object> variables = new HashMap<>();
        variables.put("virtualOrgId", virtualOrgId);
        variables.put("oldExpireDate", oldDate == null ? "" : oldDate.toString());
        variables.put("newExpireDate", newDate.toString());
        variables.put("extendReason", reason);
        request.setVariables(variables);

        processEngineAdapter.startProcess(request);
    }

    private void initiateDisbandApproval(Long virtualOrgId, String changeNo, String reason, String workSummary, String operator) {
        VirtualOrganization org = virtualOrganizationMapper.selectById(virtualOrgId);
        if (org == null) {
            return;
        }
        UnifiedProcessEngineAdapter.StartProcessRequest request = new UnifiedProcessEngineAdapter.StartProcessRequest(
            TEMPLATE_VORG_DISBAND,
            MODULE_CODE,
            virtualOrgId,
            "VORG_DISBAND",
            operator,
            operator
        );
        request.setBusinessCode(org.getVirtualOrgCode());
        request.setBusinessName(org.getVirtualOrgName());
        request.setChangeType("DISBAND");
        request.setChangeContent("Disband virtual org: " + reason);
        request.setAttachments(workSummary);
        request.setCallbackUrl("/api/virtual-org/callback");

        Map<String, Object> variables = new HashMap<>();
        variables.put("virtualOrgId", virtualOrgId);
        variables.put("changeNo", changeNo);
        variables.put("disbandReason", reason);
        variables.put("workSummary", workSummary);
        request.setVariables(variables);

        processEngineAdapter.startProcess(request);
    }
}
