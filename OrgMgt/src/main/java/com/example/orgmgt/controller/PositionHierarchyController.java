package com.example.orgmgt.controller;

import com.example.common.dto.ResponseResult;
import com.example.orgmgt.entity.PositionHierarchy;
import com.example.orgmgt.entity.PositionHierarchyHistory;
import com.example.orgmgt.service.PositionHierarchyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/position-hierarchy")
public class PositionHierarchyController {

    @Autowired
    private PositionHierarchyService hierarchyService;

    @GetMapping
    public ResponseResult<List<PositionHierarchy>> list(@RequestParam(required = false) Integer orgLevel,
                                                       @RequestParam(required = false) Long orgId,
                                                       @RequestParam(required = false) String orgCode,
                                                       @RequestParam(required = false) String hierarchyStatus) {
        List<PositionHierarchy> result;
        if (orgLevel != null) {
            result = hierarchyService.findByOrgLevel(orgLevel);
        } else if (orgId != null) {
            result = hierarchyService.findByOrgId(orgId);
        } else if (orgCode != null) {
            result = hierarchyService.findByOrgCode(orgCode);
        } else if (hierarchyStatus != null) {
            PositionHierarchy h = hierarchyService.findByHierarchyCode(hierarchyStatus);
            result = h != null ? List.of(h) : List.of();
        } else {
            result = hierarchyService.findAll();
        }
        return ResponseResult.success(result);
    }

    /**
     * Alias used by frontend list page (paging params accepted, returns all)
     */
    @GetMapping("/list")
    public ResponseResult<List<PositionHierarchy>> listAlias(@RequestParam(required = false) Integer orgLevel,
                                                             @RequestParam(required = false) Long orgId,
                                                             @RequestParam(required = false) String orgCode,
                                                             @RequestParam(required = false) String hierarchyStatus,
                                                             @RequestParam(required = false) Integer page,
                                                             @RequestParam(required = false) Integer pageSize) {
        return list(orgLevel, orgId, orgCode, hierarchyStatus);
    }

    @GetMapping("/{id}")
    public ResponseResult<PositionHierarchy> getById(@PathVariable Long id) {
        return ResponseResult.success(hierarchyService.findById(id));
    }

    @GetMapping("/code/{hierarchyCode}")
    public ResponseResult<PositionHierarchy> getByHierarchyCode(@PathVariable String hierarchyCode) {
        return ResponseResult.success(hierarchyService.findByHierarchyCode(hierarchyCode));
    }

    @PostMapping
    public ResponseResult<PositionHierarchy> create(@RequestBody PositionHierarchy hierarchy) {
        return ResponseResult.success(hierarchyService.create(hierarchy));
    }

    @PutMapping("/{id}")
    public ResponseResult<PositionHierarchy> update(@PathVariable Long id, @RequestBody PositionHierarchy hierarchy) {
        hierarchy.setId(id);
        return ResponseResult.success(hierarchyService.update(hierarchy));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        hierarchyService.deleteById(id);
        return ResponseResult.success();
    }

    @PutMapping("/{id}/status")
    public ResponseResult<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        hierarchyService.updateStatus(id, status);
        return ResponseResult.success();
    }

    @PutMapping("/{id}/appoint")
    public ResponseResult<Void> appoint(@PathVariable Long id, @RequestParam String incumbents) {
        hierarchyService.appoint(id, incumbents);
        return ResponseResult.success();
    }

    @PutMapping("/{id}/concurrent")
    public ResponseResult<Void> concurrentAppoint(@PathVariable Long id, @RequestParam String incumbents) {
        hierarchyService.concurrentAppoint(id, incumbents);
        return ResponseResult.success();
    }

    @PutMapping("/{id}/seconded")
    public ResponseResult<Void> secondedAppoint(@PathVariable Long id, @RequestParam String incumbents) {
        hierarchyService.secondedAppoint(id, incumbents);
        return ResponseResult.success();
    }

    @GetMapping("/history")
    public ResponseResult<List<PositionHierarchyHistory>> getHistory(@RequestParam(required = false) String hierarchyCode,
                                                                    @RequestParam(required = false) Integer orgLevel,
                                                                    @RequestParam(required = false) String changeType) {
        List<PositionHierarchyHistory> result;
        if (hierarchyCode != null) {
            result = hierarchyService.findHistoryByHierarchyCode(hierarchyCode);
        } else if (orgLevel != null) {
            result = hierarchyService.findHistoryByOrgLevel(orgLevel);
        } else if (changeType != null) {
            result = hierarchyService.findByChangeType(changeType);
        } else {
            result = List.of();
        }
        return ResponseResult.success(result);
    }

    @PostMapping("/batch")
    public ResponseResult<List<PositionHierarchy>> batchCreate(@RequestBody List<PositionHierarchy> hierarchies) {
        return ResponseResult.success(hierarchyService.batchCreate(hierarchies));
    }

    @PostMapping("/custom")
    public ResponseResult<PositionHierarchy> configureCustomHierarchy(@RequestParam Long orgId,
                                                                     @RequestBody PositionHierarchy hierarchy) {
        hierarchyService.configureCustomHierarchy(orgId, hierarchy);
        return ResponseResult.success(hierarchy);
    }
}