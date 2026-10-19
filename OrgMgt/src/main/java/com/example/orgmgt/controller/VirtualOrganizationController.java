package com.example.orgmgt.controller;

import com.example.orgmgt.entity.VirtualOrganization;
import com.example.orgmgt.entity.VirtualOrgMember;
import com.example.orgmgt.entity.VirtualOrgHistory;
import com.example.orgmgt.service.VirtualOrganizationService;
import com.example.common.dto.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Virtual Organization Controller
 * REST API endpoints for virtual organization lifecycle management
 */
@RestController
@RequestMapping("/api/virtual-org")
public class VirtualOrganizationController {

    @Autowired
    private VirtualOrganizationService virtualOrganizationService;

    private static final DateTimeFormatter[] DATE_TIME_FORMATS = new DateTimeFormatter[] {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
    };

    /**
     * Get virtual organization tree
     */
    @GetMapping("/tree")
    public List<Map<String, Object>> getVirtualOrgTree() {
        return virtualOrganizationService.getVirtualOrgTree();
    }

    /**
     * Get virtual organization list with filters
     */
    @GetMapping("/list")
    public ResponseResult<List<VirtualOrganization>> getVirtualOrgList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String orgType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return ResponseResult.success(virtualOrganizationService.getVirtualOrgList(keyword, orgType, status, page, pageSize));
    }

    /**
     * Get virtual organization by ID
     */
    @GetMapping("/{id}")
    public ResponseResult<VirtualOrganization> getVirtualOrgById(@PathVariable Long id) {
        return ResponseResult.success(virtualOrganizationService.getVirtualOrgById(id));
    }

    /**
     * Get virtual organization by code
     */
    @GetMapping("/code/{code}")
    public VirtualOrganization getVirtualOrgByCode(@PathVariable String code) {
        return virtualOrganizationService.getVirtualOrgByCode(code);
    }

    /**
     * Create new virtual organization (frontend entry point)
     */
    @PostMapping
    public ResponseResult<Map<String, Object>> createVirtualOrgRoot(
            @RequestBody VirtualOrganization virtualOrg,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String initiator = (userId == null || userId.isEmpty()) ? "admin" : userId;
        return ResponseResult.success(virtualOrganizationService.createVirtualOrg(virtualOrg, null, initiator));
    }

    /**
     * Create new virtual organization (initiator optional)
     */
    @PostMapping("/create")
    public ResponseResult<Map<String, Object>> createVirtualOrg(
            @RequestBody VirtualOrganization virtualOrg,
            @RequestParam(required = false) String initiator,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String op = (initiator != null && !initiator.isEmpty()) ? initiator
                : ((userId == null || userId.isEmpty()) ? "admin" : userId);
        return ResponseResult.success(virtualOrganizationService.createVirtualOrg(virtualOrg, null, op));
    }

    /**
     * Update virtual organization
     */
    @PutMapping("/{id}")
    public ResponseResult<Map<String, Object>> updateVirtualOrg(
            @PathVariable Long id,
            @RequestBody VirtualOrganization virtualOrg,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(value = "operator", required = false) String operator) {
        String op = (operator != null && !operator.isEmpty()) ? operator
                : ((userId == null || userId.isEmpty()) ? "admin" : userId);
        return ResponseResult.success(virtualOrganizationService.updateVirtualOrg(id, virtualOrg, op));
    }

    /**
     * Delete virtual organization (logical: disband)
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Map<String, Object>> deleteVirtualOrg(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "DELETE") String reason,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String operator = (userId == null || userId.isEmpty()) ? "admin" : userId;
        return ResponseResult.success(virtualOrganizationService.disbandVirtualOrg(id, reason, null, operator));
    }

    /**
     * Add member to virtual organization (legacy param endpoint)
     */
    @PostMapping("/{id}/members/add")
    public Map<String, Object> addMember(
            @PathVariable Long id,
            @RequestBody VirtualOrgMember member,
            @RequestParam(required = false) String operator,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String op = (operator != null && !operator.isEmpty()) ? operator
                : ((userId == null || userId.isEmpty()) ? "admin" : userId);
        return virtualOrganizationService.addMember(id, member, op);
    }

    /**
     * Add member to virtual organization (frontend convention: POST /{id}/members)
     */
    @PostMapping("/{id}/members")
    public ResponseResult<Map<String, Object>> addMemberDirect(
            @PathVariable Long id,
            @RequestBody VirtualOrgMember member,
            @RequestParam(required = false) String operator,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String op = (operator != null && !operator.isEmpty()) ? operator
                : ((userId == null || userId.isEmpty()) ? "admin" : userId);
        return ResponseResult.success(virtualOrganizationService.addMember(id, member, op));
    }

    /**
     * Remove member from virtual organization (frontend convention: DELETE /{id}/members with body)
     */
    @DeleteMapping("/{id}/members")
    public ResponseResult<Map<String, Object>> removeMemberByBody(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String reason,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String empId = employeeId;
        String why = reason;
        if (body != null) {
            if (empId == null && body.get("employeeId") != null) {
                empId = String.valueOf(body.get("employeeId"));
            }
            if (why == null && body.get("reason") != null) {
                why = String.valueOf(body.get("reason"));
            }
        }
        String op = (userId == null || userId.isEmpty()) ? "admin" : userId;
        return ResponseResult.success(virtualOrganizationService.removeMember(id, empId, why, op));
    }

    /**
     * Remove member from virtual organization (legacy param endpoint)
     */
    @PostMapping("/{id}/members/remove")
    public Map<String, Object> removeMember(
            @PathVariable Long id,
            @RequestParam String employeeId,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String operator,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String op = (operator != null && !operator.isEmpty()) ? operator
                : ((userId == null || userId.isEmpty()) ? "admin" : userId);
        return virtualOrganizationService.removeMember(id, employeeId, reason, op);
    }

    /**
     * Change leader
     */
    @PostMapping("/{id}/change-leader")
    public Map<String, Object> changeLeader(
            @PathVariable Long id,
            @RequestParam(required = false) String newLeaderId,
            @RequestParam(required = false) String newLeaderName,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String operator,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String op = (operator != null && !operator.isEmpty()) ? operator
                : ((userId == null || userId.isEmpty()) ? "admin" : userId);
        return virtualOrganizationService.changeLeader(id, newLeaderId, newLeaderName, reason, op);
    }

    /**
     * Extend expiration date (supports both JSON body and query params)
     */
    @PostMapping("/{id}/extend")
    public ResponseResult<Map<String, Object>> extendExpiration(
            @PathVariable Long id,
            @RequestParam(required = false) String newExpireDate,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String operator,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String dateStr = newExpireDate;
        String why = reason;
        String op = operator;
        if (body != null) {
            if ((dateStr == null || dateStr.isEmpty()) && body.get("newExpireDate") != null) {
                dateStr = String.valueOf(body.get("newExpireDate"));
            }
            if ((why == null) && body.get("reason") != null) {
                why = String.valueOf(body.get("reason"));
            }
            if ((op == null || op.isEmpty()) && body.get("operator") != null) {
                op = String.valueOf(body.get("operator"));
            }
        }
        if (op == null || op.isEmpty()) {
            op = (userId == null || userId.isEmpty()) ? "admin" : userId;
        }
        LocalDateTime parsed = parseDateTime(dateStr);
        return ResponseResult.success(virtualOrganizationService.extendExpiration(id, parsed, why, op));
    }

    /**
     * Disband virtual organization (supports both JSON body and query params)
     */
    @PostMapping("/{id}/disband")
    public ResponseResult<Map<String, Object>> disbandVirtualOrg(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String workSummaryPath,
            @RequestParam(required = false) String operator,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String why = reason;
        String summary = workSummaryPath;
        String op = operator;
        if (body != null) {
            if (why == null) {
                Object o = body.get("reason");
                if (o == null) {
                    o = body.get("disbandReason");
                }
                if (o != null) {
                    why = String.valueOf(o);
                }
            }
            if (summary == null && body.get("workSummaryPath") != null) {
                summary = String.valueOf(body.get("workSummaryPath"));
            }
            if ((op == null || op.isEmpty()) && body.get("operator") != null) {
                op = String.valueOf(body.get("operator"));
            }
        }
        if (why == null) {
            why = "项目结束，解散虚拟组织";
        }
        if (op == null || op.isEmpty()) {
            op = (userId == null || userId.isEmpty()) ? "admin" : userId;
        }
        return ResponseResult.success(virtualOrganizationService.disbandVirtualOrg(id, why, summary, op));
    }

    /**
     * Get members of virtual organization
     */
    @GetMapping("/{id}/members")
    public ResponseResult<List<VirtualOrgMember>> getMembers(@PathVariable Long id) {
        return ResponseResult.success(virtualOrganizationService.getMembers(id));
    }

    /**
     * Get history of virtual organization by ID (frontend convention)
     */
    @GetMapping("/{id}/history")
    public ResponseResult<List<VirtualOrgHistory>> getHistoryByOrgId(@PathVariable Long id) {
        VirtualOrganization org = virtualOrganizationService.getVirtualOrgById(id);
        if (org == null) {
            return ResponseResult.success(new ArrayList<>());
        }
        return ResponseResult.success(virtualOrganizationService.getHistory(org.getVirtualOrgCode(), null, 1, 100));
    }

    /**
     * Get expiring virtual organizations
     */
    @GetMapping("/expiring")
    public List<VirtualOrganization> getExpiringOrgs(@RequestParam(defaultValue = "30") int daysThreshold) {
        return virtualOrganizationService.getExpiringOrgs(daysThreshold);
    }

    /**
     * Get history
     */
    @GetMapping("/history")
    public List<VirtualOrgHistory> getHistory(
            @RequestParam(required = false) String virtualOrgCode,
            @RequestParam(required = false) String changeType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return virtualOrganizationService.getHistory(virtualOrgCode, changeType, page, pageSize);
    }

    /**
     * Get history by ID
     */
    @GetMapping("/history/{id}")
    public VirtualOrgHistory getHistoryById(@PathVariable Long id) {
        return virtualOrganizationService.getHistoryById(id);
    }

    /**
     * Get virtual organizations for employee
     */
    @GetMapping("/employee/{employeeId}")
    public List<VirtualOrganization> getVirtualOrgsByEmployee(@PathVariable String employeeId) {
        return virtualOrganizationService.getVirtualOrgsByEmployee(employeeId);
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public Map<String, Object> getVirtualOrgStats() {
        return virtualOrganizationService.getVirtualOrgStats();
    }

    /**
     * Batch import members
     */
    @PostMapping("/{id}/members/batch-import")
    public Map<String, Object> batchImportMembers(
            @PathVariable Long id,
            @RequestBody List<Map<String, Object>> memberList,
            @RequestParam(required = false) String operator,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String op = (operator != null && !operator.isEmpty()) ? operator
                : ((userId == null || userId.isEmpty()) ? "admin" : userId);
        return virtualOrganizationService.batchImportMembers(id, memberList, op);
    }

    /**
     * Batch export
     */
    @PostMapping("/batch-export")
    public List<VirtualOrganization> batchExport(@RequestBody List<Long> virtualOrgIds) {
        return virtualOrganizationService.batchExport(virtualOrgIds);
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String v = value.trim();
        try {
            return LocalDate.parse(v, DateTimeFormatter.ISO_LOCAL_DATE).atTime(23, 59, 59);
        } catch (Exception ignored) {
            // try date-time formats below
        }
        for (DateTimeFormatter f : DATE_TIME_FORMATS) {
            try {
                return LocalDateTime.parse(v, f);
            } catch (Exception ignored) {
                // try next
            }
        }
        throw new IllegalArgumentException("无法解析日期: " + value);
    }
}
