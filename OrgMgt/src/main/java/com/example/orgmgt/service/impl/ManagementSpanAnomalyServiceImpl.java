package com.example.orgmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.orgmgt.entity.ManagementSpanAnomaly;
import com.example.orgmgt.entity.ManagementSpanStandard;
import com.example.orgmgt.mapper.ManagementSpanAnomalyMapper;
import com.example.orgmgt.service.ManagementSpanAnomalyService;
import com.example.orgmgt.service.ManagementSpanStandardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Management Span Anomaly Service Implementation
 * Implements three-color warning system and anomaly tracking
 */
@Service
public class ManagementSpanAnomalyServiceImpl extends ServiceImpl<ManagementSpanAnomalyMapper, ManagementSpanAnomaly> 
    implements ManagementSpanAnomalyService {
    
    @Autowired
    private ManagementSpanStandardService spanStandardService;
    
    @Override
    public List<ManagementSpanAnomaly> getByWarningLevel(String warningLevel) {
        return baseMapper.selectByWarningLevel(warningLevel);
    }
    
    @Override
    public List<ManagementSpanAnomaly> getByStatus(String status) {
        return baseMapper.selectByStatus(status);
    }
    
    @Override
    public List<ManagementSpanAnomaly> getByOrgId(Long orgId) {
        return baseMapper.selectByOrgId(orgId);
    }
    
    @Override
    public List<ManagementSpanAnomaly> getUnresolved() {
        LambdaQueryWrapper<ManagementSpanAnomaly> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ManagementSpanAnomaly::getStatus, "PENDING", "PROCESSING");
        return this.list(wrapper);
    }
    
    @Override
    @Transactional
    public boolean createAnomaly(ManagementSpanAnomaly anomaly) {
        anomaly.setOccurrenceTime(LocalDateTime.now());
        anomaly.setStatus("PENDING");
        return this.save(anomaly);
    }
    
    @Override
    @Transactional
    public boolean updateStatus(Long id, String status, String remark) {
        ManagementSpanAnomaly anomaly = this.getById(id);
        if (anomaly == null) return false;
        anomaly.setStatus(status);
        if (remark != null) anomaly.setRemark(remark);
        return this.updateById(anomaly);
    }
    
    @Override
    @Transactional
    public boolean resolveAnomaly(Long id, String rectificationDesc) {
        ManagementSpanAnomaly anomaly = this.getById(id);
        if (anomaly == null) return false;
        anomaly.setStatus("RESOLVED");
        anomaly.setResolveTime(LocalDateTime.now());
        anomaly.setRectificationDesc(rectificationDesc);
        return this.updateById(anomaly);
    }
    
    @Override
    @Transactional
    public boolean applyExemption(Long id, String exemptionReason, String approvalNo) {
        ManagementSpanAnomaly anomaly = this.getById(id);
        if (anomaly == null) return false;
        anomaly.setExemptionApprovalNo(approvalNo);
        anomaly.setStatus("EXEMPTED");
        return this.updateById(anomaly);
    }
    
    @Override
    public Map<String, Object> getAnomalyStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("critical", baseMapper.countPendingByLevel("RED"));
        stats.put("warning", baseMapper.countPendingByLevel("YELLOW"));
        stats.put("total", this.count());
        stats.put("resolved", baseMapper.selectByStatus("RESOLVED").size());
        return stats;
    }
    
    @Override
    @Transactional
    public boolean validateAndCreateAnomaly(Long positionId, String positionLevel, Integer currentSpan) {
        ManagementSpanStandard standard = spanStandardService.getByPositionLevel(positionLevel);
        if (standard == null) return true;
        
        String anomalyType = null;
        String warningLevel = "GREEN";
        
        // Check if exceeds maximum
        if (standard.getMaxSpan() != null && currentSpan > standard.getMaxSpan()) {
            anomalyType = "AMPLITUDE_HIGH";
            warningLevel = "RED";
        } else if (standard.getMinSpan() != null && currentSpan < standard.getMinSpan()) {
            anomalyType = "AMPLITUDE_LOW";
            warningLevel = "RED";
        } else if (standard.getStandardSpan() != null && currentSpan > standard.getStandardSpan()) {
            warningLevel = "YELLOW";
        }
        
        if (anomalyType != null) {
            ManagementSpanAnomaly anomaly = new ManagementSpanAnomaly();
            anomaly.setPositionId(positionId);
            anomaly.setPositionLevel(positionLevel);
            anomaly.setSubordinateCount(currentSpan);
            anomaly.setMinAmplitude(standard.getMinSpan());
            anomaly.setStandardAmplitude(standard.getStandardSpan());
            anomaly.setMaxAmplitude(standard.getMaxSpan());
            anomaly.setAnomalyType(anomalyType);
            anomaly.setWarningLevel(warningLevel);
            anomaly.setOccurrenceTime(LocalDateTime.now());
            anomaly.setStatus("PENDING");
            return this.save(anomaly);
        }
        return true;
    }
}
