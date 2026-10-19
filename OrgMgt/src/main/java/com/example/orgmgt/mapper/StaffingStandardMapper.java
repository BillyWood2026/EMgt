package com.example.orgmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orgmgt.entity.StaffingStandard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StaffingStandardMapper extends BaseMapper<StaffingStandard> {
    
    @Select("SELECT * FROM staffing_standard WHERE org_id=#{orgId} AND deleted=0")
    List<StaffingStandard> selectByOrgId(@Param("orgId") Long orgId);
    
    @Select("SELECT * FROM staffing_standard WHERE status='ACTIVE' AND deleted=0")
    List<StaffingStandard> selectActiveStandards();
    
    @Select("SELECT * FROM staffing_standard WHERE position_level=#{positionLevel} AND deleted=0")
    List<StaffingStandard> selectByPositionLevel(@Param("positionLevel") String positionLevel);
    
    @Select("SELECT * FROM staffing_standard WHERE staffing_type=#{staffingType} AND deleted=0")
    List<StaffingStandard> selectByStaffingType(@Param("staffingType") String staffingType);
    
    @Select("SELECT * FROM staffing_standard WHERE org_id=#{orgId} AND position_level=#{positionLevel} AND deleted=0")
    List<StaffingStandard> selectByOrgAndLevel(@Param("orgId") Long orgId, @Param("positionLevel") String positionLevel);
}
