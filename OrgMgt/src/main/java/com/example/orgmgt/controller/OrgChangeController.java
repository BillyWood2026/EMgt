package com.example.orgmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.orgmgt.entity.OrgChangeApproval;
import com.example.orgmgt.entity.OrgChangeHistory;
import com.example.orgmgt.mapper.OrgChangeHistoryMapper;
import com.example.orgmgt.service.OrgChangeService;
import com.example.common.dto.ResponseResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Organization Change Controller
 * REST API endpoints for change approval and history management
 */
@RestController
@RequestMapping("/api/org-change")
public class OrgChangeController {
    
    @Autowired
    private OrgChangeService orgChangeService;

    @Autowired
    private OrgChangeHistoryMapper orgChangeHistoryMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // ================= Generic CRUD used by frontend change page =================

    @GetMapping("/list")
    public ResponseResult<List<OrgChangeHistory>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String changeType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orgCode,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        LambdaQueryWrapper<OrgChangeHistory> wrapper = Wrappers.<OrgChangeHistory>lambdaQuery()
                .and(keyword != null && !keyword.trim().isEmpty(),
                        w -> w.like(OrgChangeHistory::getChangeTitle, keyword)
                                .or().like(OrgChangeHistory::getOrgName, keyword)
                                .or().like(OrgChangeHistory::getChangeNo, keyword))
                .eq(changeType != null && !changeType.trim().isEmpty(),
                        OrgChangeHistory::getChangeType, changeType)
                .eq(status != null && !status.trim().isEmpty(),
                        OrgChangeHistory::getApprovalStatus, status)
                .eq(orgCode != null && !orgCode.trim().isEmpty(),
                        OrgChangeHistory::getOrgCode, orgCode)
                .orderByDesc(OrgChangeHistory::getCreatedTime);
        return ResponseResult.success(orgChangeHistoryMapper.selectList(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<OrgChangeHistory> getById(@PathVariable Long id) {
        return ResponseResult.success(orgChangeHistoryMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<OrgChangeHistory> create(@RequestBody Map<String, Object> params,
                                                    @RequestHeader(value = "X-User-Id", required = false) String userId) {
        OrgChangeHistory entity = objectMapper.convertValue(params, OrgChangeHistory.class);
        if (entity.getChangeNo() == null || entity.getChangeNo().trim().isEmpty()) {
            entity.setChangeNo("CHG" + System.currentTimeMillis());
        }
        if (entity.getOrgId() == null) {
            entity.setOrgId(0L);
        }
        if (entity.getChangeType() == null || entity.getChangeType().trim().isEmpty()) {
            entity.setChangeType("CREATE");
        }
        if (entity.getApprovalStatus() == null || entity.getApprovalStatus().trim().isEmpty()) {
            entity.setApprovalStatus("DRAFT");
        }
        String operator = (userId == null || userId.isEmpty()) ? "admin" : userId;
        entity.setCreatedBy(operator);
        entity.setCreatedTime(LocalDateTime.now());
        entity.setDeleted(0);
        entity.setVersion(1);
        orgChangeHistoryMapper.insert(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<OrgChangeHistory> update(@PathVariable Long id, @RequestBody Map<String, Object> params,
                                                    @RequestHeader(value = "X-User-Id", required = false) String userId) {
        OrgChangeHistory entity = objectMapper.convertValue(params, OrgChangeHistory.class);
        entity.setId(id);
        entity.setUpdatedBy((userId == null || userId.isEmpty()) ? "admin" : userId);
        entity.setUpdatedTime(LocalDateTime.now());
        orgChangeHistoryMapper.updateById(entity);
        return ResponseResult.success(entity);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> remove(@PathVariable Long id) {
        orgChangeHistoryMapper.deleteById(id);
        return ResponseResult.success();
    }

    @PostMapping("/{id}/submit")
    public ResponseResult<Void> submit(@PathVariable Long id) {
        orgChangeHistoryMapper.updateApprovalStatus(id, "PENDING");
        return ResponseResult.success();
    }

    // ================= Approval / history workflows =================

    @GetMapping("/approval/list")
    public ResponseResult<List<OrgChangeApproval>> getApprovalList(
            @RequestParam(required = false) String orgCode,
            @RequestParam(required = false) String changeType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        List<OrgChangeApproval> list = orgChangeService.getApprovalList(orgCode, changeType, status, page, pageSize);
        return ResponseResult.success(list);
    }
    
    @GetMapping("/approval/{id}")
    public ResponseResult<OrgChangeApproval> getApprovalById(@PathVariable Long id) {
        OrgChangeApproval approval = orgChangeService.getApprovalById(id);
        return ResponseResult.success(approval);
    }
    
    @PostMapping("/approval")
    public ResponseResult<Map<String, Object>> createApproval(@RequestBody Map<String, Object> params,
                                                            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Long orgId = ((Number) params.get("orgId")).longValue();
        String changeType = (String) params.get("changeType");
        String changeContent = (String) params.get("changeContent");
        String beforeData = (String) params.get("beforeData");
        String afterData = (String) params.get("afterData");
        
        Map<String, Object> result = orgChangeService.createApproval(orgId, changeType, changeContent, beforeData, afterData, userId);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseResult.success(result);
        } else {
            return ResponseResult.error((String) result.get("message"));
        }
    }
    
    @PostMapping("/approval/{id}/approve")
    public ResponseResult<Map<String, Object>> approve(@PathVariable Long id,
                                                     @RequestBody Map<String, Object> params,
                                                     @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String opinion = (String) params.get("opinion");
        Map<String, Object> result = orgChangeService.approve(id, opinion, userId);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseResult.success(result);
        } else {
            return ResponseResult.error((String) result.get("message"));
        }
    }
    
    @PostMapping("/approval/{id}/reject")
    public ResponseResult<Map<String, Object>> reject(@PathVariable Long id,
                                                     @RequestBody Map<String, Object> params,
                                                     @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String reason = (String) params.get("reason");
        Map<String, Object> result = orgChangeService.reject(id, reason, userId);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseResult.success(result);
        } else {
            return ResponseResult.error((String) result.get("message"));
        }
    }
    
    @PostMapping("/approval/{id}/recall")
    public ResponseResult<Map<String, Object>> recall(@PathVariable Long id,
                                                     @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Map<String, Object> result = orgChangeService.recall(id, userId);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseResult.success(result);
        } else {
            return ResponseResult.error((String) result.get("message"));
        }
    }
    
    @GetMapping("/history/list")
    public ResponseResult<List<OrgChangeHistory>> getHistoryList(
            @RequestParam(required = false) String orgCode,
            @RequestParam(required = false) String changeType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        List<OrgChangeHistory> list = orgChangeService.getHistoryList(orgCode, changeType, startTime, endTime, page, pageSize);
        return ResponseResult.success(list);
    }
    
    @GetMapping("/history/{id}")
    public ResponseResult<OrgChangeHistory> getHistoryById(@PathVariable Long id) {
        OrgChangeHistory history = orgChangeService.getHistoryById(id);
        return ResponseResult.success(history);
    }
    
    @GetMapping("/compare")
    public ResponseResult<Map<String, Object>> compareVersions(@RequestParam Long historyId1, @RequestParam Long historyId2) {
        Map<String, Object> result = orgChangeService.compareVersions(historyId1, historyId2);
        return ResponseResult.success(result);
    }
    
    @GetMapping("/change-types")
    public ResponseResult<List<Map<String, Object>>> getChangeTypes() {
        List<Map<String, Object>> types = orgChangeService.getChangeTypes();
        return ResponseResult.success(types);
    }
    
    @GetMapping("/stats")
    public ResponseResult<Map<String, Object>> getApprovalStats() {
        Map<String, Object> stats = orgChangeService.getApprovalStats();
        return ResponseResult.success(stats);
    }
    
    @PostMapping("/history/export")
    public ResponseResult<List<OrgChangeHistory>> batchExportHistory(@RequestBody List<Long> historyIds) {
        List<OrgChangeHistory> result = orgChangeService.batchExportHistory(historyIds);
        return ResponseResult.success(result);
    }
}