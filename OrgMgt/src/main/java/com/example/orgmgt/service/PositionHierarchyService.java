package com.example.orgmgt.service;

import com.example.orgmgt.entity.PositionHierarchy;
import com.example.orgmgt.entity.PositionHierarchyHistory;

import java.util.List;

public interface PositionHierarchyService {
    
    List<PositionHierarchy> findAll();
    
    PositionHierarchy findById(Long id);
    
    PositionHierarchy findByHierarchyCode(String hierarchyCode);
    
    List<PositionHierarchy> findByOrgLevel(Integer orgLevel);
    
    List<PositionHierarchy> findByOrgId(Long orgId);
    
    List<PositionHierarchy> findByOrgCode(String orgCode);
    
    PositionHierarchy create(PositionHierarchy hierarchy);
    
    PositionHierarchy update(PositionHierarchy hierarchy);
    
    void deleteById(Long id);
    
    void updateStatus(Long id, String status);
    
    void appoint(Long hierarchyId, String incumbents);
    
    void concurrentAppoint(Long hierarchyId, String incumbents);
    
    void secondedAppoint(Long hierarchyId, String incumbents);
    
    PositionHierarchyHistory createChangeRecord(String hierarchyCode, String changeType, 
                                               String beforeContent, String afterContent);
    
    List<PositionHierarchyHistory> findHistoryByHierarchyCode(String hierarchyCode);
    
    List<PositionHierarchyHistory> findHistoryByOrgLevel(Integer orgLevel);
    
    List<PositionHierarchyHistory> findByChangeType(String changeType);
    
    List<PositionHierarchy> batchCreate(List<PositionHierarchy> hierarchies);
    
    void configureCustomHierarchy(Long orgId, PositionHierarchy hierarchy);
}