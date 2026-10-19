package com.example.orgmgt.controller;

import com.example.orgmgt.entity.OrgUnit;
import com.example.orgmgt.service.OrgUnitService;
import com.example.common.dto.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Organization Unit Controller
 * REST API endpoints for five-level organization management
 */
@RestController
@RequestMapping("/api/org-unit")
public class OrgUnitController {
    
    @Autowired
    private OrgUnitService orgUnitService;
    
    @GetMapping("/tree")
    public ResponseResult<List<Map<String, Object>>> getOrgTree() {
        List<Map<String, Object>> tree = orgUnitService.getOrgTree();
        return ResponseResult.success(tree);
    }
    
    @GetMapping("/list")
    public ResponseResult<List<OrgUnit>> getOrgList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String orgLevel,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        List<OrgUnit> list = orgUnitService.getOrgList(keyword, orgLevel, status, page, pageSize);
        return ResponseResult.success(list);
    }
    
    @GetMapping("/{id}")
    public ResponseResult<OrgUnit> getOrgById(@PathVariable Long id) {
        OrgUnit orgUnit = orgUnitService.getOrgById(id);
        return ResponseResult.success(orgUnit);
    }
    
    @GetMapping("/code/{code}")
    public ResponseResult<OrgUnit> getOrgByCode(@PathVariable String code) {
        OrgUnit orgUnit = orgUnitService.getOrgByCode(code);
        return ResponseResult.success(orgUnit);
    }
    
    @PostMapping
    public ResponseResult<Map<String, Object>> createOrg(@RequestBody OrgUnit orgUnit,
                                                        @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Map<String, Object> result = orgUnitService.createOrg(orgUnit, userId);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseResult.success(result);
        } else {
            return ResponseResult.error((String) result.get("message"));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseResult<Map<String, Object>> updateOrg(@PathVariable Long id, 
                                                        @RequestBody OrgUnit orgUnit,
                                                        @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Map<String, Object> result = orgUnitService.updateOrg(id, orgUnit, userId);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseResult.success(result);
        } else {
            return ResponseResult.error((String) result.get("message"));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseResult<Map<String, Object>> deleteOrg(@PathVariable Long id,
                                                        @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Map<String, Object> result = orgUnitService.deleteOrg(id, userId);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseResult.success(result);
        } else {
            return ResponseResult.error((String) result.get("message"));
        }
    }
    
    @PostMapping("/{id}/status")
    public ResponseResult<Map<String, Object>> toggleStatus(@PathVariable Long id,
                                                           @RequestParam String status,
                                                           @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Map<String, Object> result = orgUnitService.toggleStatus(id, status, userId);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseResult.success(result);
        } else {
            return ResponseResult.error((String) result.get("message"));
        }
    }
    
    @PostMapping("/{id}/move")
    public ResponseResult<Map<String, Object>> moveOrg(@PathVariable Long id,
                                                      @RequestParam Long newParentId,
                                                      @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Map<String, Object> result = orgUnitService.moveOrg(id, newParentId, userId);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseResult.success(result);
        } else {
            return ResponseResult.error((String) result.get("message"));
        }
    }
    
    @PostMapping("/validate")
    public ResponseResult<Map<String, Object>> validateCreate(@RequestBody OrgUnit orgUnit) {
        Map<String, Object> result = orgUnitService.validateCreate(orgUnit);
        return ResponseResult.success(result);
    }
    
    @GetMapping("/generate-code")
    public ResponseResult<String> generateOrgCode(@RequestParam(required = false) Long parentId) {
        String code = orgUnitService.generateOrgCode(parentId);
        return ResponseResult.success(code);
    }
    
    @GetMapping("/{id}/has-children")
    public ResponseResult<Boolean> hasChildren(@PathVariable Long id) {
        boolean has = orgUnitService.hasChildren(id);
        return ResponseResult.success(has);
    }
    
    @GetMapping("/stats")
    public ResponseResult<Map<String, Object>> getOrgStats() {
        Map<String, Object> stats = orgUnitService.getOrgStats();
        return ResponseResult.success(stats);
    }
    
    @PostMapping("/batch-import")
    public ResponseResult<Map<String, Object>> batchImport(@RequestBody List<Map<String, Object>> dataList) {
        Map<String, Object> result = orgUnitService.batchImport(dataList);
        return ResponseResult.success(result);
    }
    
    @PostMapping("/batch-export")
    public ResponseResult<List<OrgUnit>> batchExport(@RequestBody List<Long> orgIds) {
        List<OrgUnit> result = orgUnitService.batchExport(orgIds);
        return ResponseResult.success(result);
    }
}