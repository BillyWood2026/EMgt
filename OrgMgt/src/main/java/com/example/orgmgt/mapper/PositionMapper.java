package com.example.orgmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orgmgt.entity.Position;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PositionMapper extends BaseMapper<Position> {

    @Insert("INSERT INTO position (position_code, position_name, org_id, org_code, org_name, position_level, " +
            "position_rank, position_short_name, position_category, salary_grade, owner, position_duty, staffing_type, staffing_count, current_count, position_status, " +
            "responsibilities, qualifications, effective_date, expire_date, flexible_quota_expire_date, " +
            "sort_order, remark, created_by, created_time, updated_by, updated_time, deleted, version) " +
            "VALUES (#{positionCode}, #{positionName}, #{orgId}, #{orgCode}, #{orgName}, #{positionLevel}, " +
            "#{positionRank}, #{positionShortName}, #{positionCategory}, #{salaryGrade}, #{owner}, #{positionDuty}, #{staffingType}, #{staffingCount}, #{currentCount}, #{positionStatus}, " +
            "#{responsibilities}, #{qualifications}, #{effectiveDate}, #{expireDate}, #{flexibleQuotaExpireDate}, " +
            "#{sortOrder}, #{remark}, #{createdBy}, #{createdTime}, #{updatedBy}, #{updatedTime}, 0, 1)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Position position);

    @Update("UPDATE position SET position_code=#{positionCode}, position_name=#{positionName}, org_id=#{orgId}, " +
            "org_code=#{orgCode}, org_name=#{orgName}, position_level=#{positionLevel}, position_rank=#{positionRank}, " +
            "position_short_name=#{positionShortName}, position_category=#{positionCategory}, salary_grade=#{salaryGrade}, " +
            "owner=#{owner}, position_duty=#{positionDuty}, staffing_type=#{staffingType}, staffing_count=#{staffingCount}, " +
            "current_count=#{currentCount}, position_status=#{positionStatus}, responsibilities=#{responsibilities}, " +
            "qualifications=#{qualifications}, effective_date=#{effectiveDate}, expire_date=#{expireDate}, " +
            "flexible_quota_expire_date=#{flexibleQuotaExpireDate}, sort_order=#{sortOrder}, remark=#{remark}, " +
            "updated_by=#{updatedBy}, updated_time=#{updatedTime}, version=#{version} WHERE id=#{id}")
    int update(Position position);

    @Update("UPDATE position SET deleted=1 WHERE id=#{id}")
    int deleteById(Long id);
    
    @Select("SELECT * FROM position WHERE deleted=0")
    List<Position> findAll();
    
    @Select("SELECT * FROM position WHERE id=#{id} AND deleted=0")
    Position findById(Long id);
    
    @Select("SELECT * FROM position WHERE position_code=#{positionCode} AND deleted=0")
    Position findByPositionCode(String positionCode);
    
    @Select("SELECT * FROM position WHERE org_id=#{orgId} AND deleted=0 ORDER BY sort_order")
    List<Position> findByOrgId(Long orgId);
    
    @Select("SELECT * FROM position WHERE org_code=#{orgCode} AND deleted=0 ORDER BY sort_order")
    List<Position> findByOrgCode(String orgCode);
    
    @Select("SELECT * FROM position WHERE position_level=#{positionLevel} AND deleted=0 ORDER BY sort_order")
    List<Position> findByPositionLevel(String positionLevel);
    
    @Select("SELECT * FROM position WHERE position_status=#{positionStatus} AND deleted=0")
    List<Position> findByPositionStatus(String positionStatus);
    
    @Update("UPDATE position SET position_status=#{status} WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    @Update("UPDATE position SET current_count=#{count} WHERE id=#{id}")
    int updateCurrentCount(@Param("id") Long id, @Param("count") Integer count);
    
    @Select("SELECT * FROM position WHERE position_status='ENABLED' AND current_count < staffing_count AND deleted=0 ORDER BY sort_order")
    List<Position> findVacantPositions();
}
