package com.example.orgmgt.controller;

import com.example.common.dto.ResponseResult;
import com.example.orgmgt.entity.Organization;
import com.example.orgmgt.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/org")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @GetMapping("/tree")
    public ResponseResult<List<Map<String, Object>>> getOrgTree() {
        return ResponseResult.success(organizationService.getOrgTree());
    }

    @GetMapping({"", "/list"})
    public ResponseResult<List<Organization>> list(@RequestParam(required = false) Integer orgLevel,
                                                  @RequestParam(required = false) String orgStatus,
                                                  @RequestParam(required = false) String orgName) {
        List<Organization> result;
        if (orgLevel != null) {
            result = organizationService.findByOrgLevel(orgLevel);
        } else if (orgStatus != null) {
            result = organizationService.findByOrgStatus(orgStatus);
        } else if (orgName != null) {
            result = organizationService.findByOrgNameLike(orgName);
        } else {
            result = organizationService.findAll();
        }
        return ResponseResult.success(result);
    }

    @GetMapping("/{id}")
    public ResponseResult<Organization> getById(@PathVariable Long id) {
        return ResponseResult.success(organizationService.findById(id));
    }

    @GetMapping("/code/{orgCode}")
    public ResponseResult<Organization> getByOrgCode(@PathVariable String orgCode) {
        return ResponseResult.success(organizationService.findByOrgCode(orgCode));
    }

    @GetMapping("/parent/{parentId}")
    public ResponseResult<List<Organization>> getByParentId(@PathVariable Long parentId) {
        return ResponseResult.success(organizationService.findByParentOrgId(parentId));
    }

    @PostMapping
    public ResponseResult<Organization> create(@RequestBody Organization organization) {
        return ResponseResult.success(organizationService.create(organization));
    }

    @PutMapping("/{id}")
    public ResponseResult<Organization> update(@PathVariable Long id, @RequestBody Organization organization) {
        organization.setId(id);
        return ResponseResult.success(organizationService.update(organization));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        organizationService.deleteById(id);
        return ResponseResult.success();
    }

    @PutMapping("/{id}/status")
    public ResponseResult<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        organizationService.updateStatus(id, status);
        return ResponseResult.success();
    }

    @PutMapping("/{id}/migrate")
    public ResponseResult<Void> migrate(@PathVariable Long id, 
                                        @RequestParam Long newParentId,
                                        @RequestParam(required = false) String changeReason,
                                        @RequestParam(required = false) String attachmentPath) {
        // For backward compatibility, use empty initiator
        // In real implementation, get initiator from security context
        organizationService.migrateOrg(id, newParentId, changeReason, attachmentPath, "SYSTEM");
        return ResponseResult.success();
    }

    @PostMapping("/batch")
    public ResponseResult<List<Organization>> batchCreate(@RequestBody List<Organization> organizations) {
        return ResponseResult.success(organizationService.batchCreate(organizations));
    }

    @DeleteMapping("/batch")
    public ResponseResult<Void> batchDelete(@RequestBody List<Long> ids) {
        organizationService.batchDelete(ids);
        return ResponseResult.success();
    }

    @GetMapping("/generate-code")
    public ResponseResult<String> generateCode(@RequestParam(required = false) Long parentOrgId,
                                               @RequestParam Integer orgLevel) {
        return ResponseResult.success(organizationService.generateOrgCode(parentOrgId, orgLevel));
    }
}