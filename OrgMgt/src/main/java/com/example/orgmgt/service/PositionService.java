package com.example.orgmgt.service;

import com.example.orgmgt.entity.Position;

import java.util.List;

public interface PositionService {
    
    List<Position> findAll();
    
    Position findById(Long id);
    
    Position findByPositionCode(String positionCode);
    
    List<Position> findByOrgId(Long orgId);
    
    List<Position> findByOrgCode(String orgCode);
    
    List<Position> findByPositionLevel(String positionLevel);
    
    List<Position> findByPositionStatus(String positionStatus);
    
    Position create(Position position);
    
    Position update(Position position);
    
    void deleteById(Long id);
    
    void updateStatus(Long id, String status);
    
    void updateCurrentCount(Long id, Integer count);
    
    List<Position> findVacantPositions();
    
    List<Position> batchCreate(List<Position> positions);
    
    void batchDelete(List<Long> ids);
    
    void batchUpdateOrg(List<Long> positionIds, Long newOrgId);
}