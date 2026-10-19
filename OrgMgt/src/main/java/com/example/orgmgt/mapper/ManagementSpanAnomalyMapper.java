package com.example.orgmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orgmgt.entity.ManagementSpanAnomaly;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ManagementSpanAnomalyMapper extends BaseMapper<ManagementSpanAnomaly> {
    
    @Select("SELECT * FROM management_span_anomaly WHERE warning_level=#{warningLevel} AND deleted=0 ORDER BY occurrence_time DESC")
    List<ManagementSpanAnomaly> selectByWarningLevel(@Param("warningLevel") String warningLevel);
    
    @Select("SELECT * FROM management_span_anomaly WHERE status=#{status} AND deleted=0 ORDER BY occurrence_time DESC")
    List<ManagementSpanAnomaly> selectByStatus(@Param("status") String status);
    
    @Select("SELECT * FROM management_span_anomaly WHERE org_id=#{orgId} AND deleted=0 ORDER BY occurrence_time DESC")
    List<ManagementSpanAnomaly> selectByOrgId(@Param("orgId") Long orgId);
    
    @Select("SELECT * FROM management_span_anomaly WHERE anomaly_type=#{anomalyType} AND deleted=0 ORDER BY occurrence_time DESC")
    List<ManagementSpanAnomaly> selectByAnomalyType(@Param("anomalyType") String anomalyType);
    
    @Select("SELECT COUNT(*) FROM management_span_anomaly WHERE warning_level=#{warningLevel} AND status='PENDING' AND deleted=0")
    int countPendingByLevel(@Param("warningLevel") String warningLevel);
    
    @Select("SELECT * FROM management_span_anomaly WHERE status IN ('PENDING', 'PROCESSING') AND deleted=0 ORDER BY occurrence_time DESC")
    List<ManagementSpanAnomaly> selectUnresolved();
}
