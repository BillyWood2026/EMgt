package com.example.orgmgt.service;

import com.example.orgmgt.entity.Organization;

import java.util.List;
import java.util.Map;

public interface OrganizationService {
    
    List<Organization> findAll();
    
    Organization findById(Long id);
    
    Organization findByOrgCode(String orgCode);
    
    List<Organization> findByOrgLevel(Integer orgLevel);
    
    List<Organization> findByParentOrgId(Long parentOrgId);
    
    List<Organization> findByOrgStatus(String orgStatus);
    
    Organization create(Organization organization);
    
    Organization update(Organization organization);
    
    void deleteById(Long id);
    
    void updateStatus(Long id, String status);
    
    List<Organization> findByOrgNameLike(String orgName);
    
    List<Organization> findByBusinessLine(String businessLine);
    
    String generateOrgCode(Long parentOrgId, Integer orgLevel);
    
    Map<String, Object> migrateOrg(Long orgId, Long newParentOrgId, String changeReason, 
                                   String attachmentPath, String initiator);
    
    List<Map<String, Object>> getOrgTree();
    
    List<Organization> batchCreate(List<Organization> organizations);
    
    void batchDelete(List<Long> ids);
}