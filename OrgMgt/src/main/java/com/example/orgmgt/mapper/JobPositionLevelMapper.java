package com.example.orgmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orgmgt.entity.JobPositionLevel;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

/**
 * Job Position Level Mapper
 * Data access interface for five-level job position hierarchy management
 * Supports CRUD operations and complex queries for state-owned enterprise compliance
 */
@Mapper
public interface JobPositionLevelMapper extends BaseMapper<JobPositionLevel> {
    
    /**
     * Insert new job position
     */
    @Insert("INSERT INTO job_position_level (position_code, position_name, org_level, org_id, org_code, org_name, " +
            "position_grade, position_status, incumbent_id, incumbent_name, effective_date, expire_date, " +
            "responsibility_desc, sort_order, is_core_management, is_temporary_post, is_part_time_post, " +
            "is_personalized, personalized_name, personalized_reason, is_in_staffing_control, is_in_command_chain, " +
            "remarks, created_by, create_time, updated_by, update_time) " +
            "VALUES (#{positionCode}, #{positionName}, #{orgLevel}, #{orgId}, #{orgCode}, #{orgName}, " +
            "#{positionGrade}, #{positionStatus}, #{incumbentId}, #{incumbentName}, #{effectiveDate}, #{expireDate}, " +
            "#{responsibilityDesc}, #{sortOrder}, #{isCoreManagement}, #{isTemporaryPost}, #{isPartTimePost}, " +
            "#{isPersonalized}, #{personalizedName}, #{personalizedReason}, #{isInStaffingControl}, #{isInCommandChain}, " +
            "#{remarks}, #{createdBy}, #{createTime}, #{updatedBy}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(JobPositionLevel position);
    
    /**
     * Update job position
     */
    @Update("UPDATE job_position_level SET position_name = #{positionName}, position_grade = #{positionGrade}, " +
            "position_status = #{positionStatus}, incumbent_id = #{incumbentId}, incumbent_name = #{incumbentName}, " +
            "expire_date = #{expireDate}, responsibility_desc = #{responsibilityDesc}, sort_order = #{sortOrder}, " +
            "is_core_management = #{isCoreManagement}, is_temporary_post = #{isTemporaryPost}, " +
            "is_part_time_post = #{isPartTimePost}, is_personalized = #{isPersonalized}, " +
            "personalized_name = #{personalizedName}, personalized_reason = #{personalizedReason}, " +
            "is_in_staffing_control = #{isInStaffingControl}, is_in_command_chain = #{isInCommandChain}, " +
            "remarks = #{remarks}, updated_by = #{updatedBy}, update_time = #{updateTime} " +
            "WHERE id = #{id}")
    int update(JobPositionLevel position);
    
    /**
     * Delete job position (soft delete by setting status to ABOLISHED)
     */
    @Update("UPDATE job_position_level SET position_status = 'ABOLISHED', expire_date = NOW(), " +
            "updated_by = #{operator}, update_time = NOW() WHERE id = #{id}")
    int abolish(@Param("id") Long id, @Param("operator") String operator);
    
    /**
     * Select by ID
     */
    @Select("SELECT * FROM job_position_level WHERE id = #{id}")
    JobPositionLevel selectById(@Param("id") Long id);
    
    /**
     * Select by position code
     */
    @Select("SELECT * FROM job_position_level WHERE position_code = #{positionCode}")
    JobPositionLevel selectByCode(@Param("positionCode") String positionCode);
    
    /**
     * Select all positions with filters
     */
    @Select("<script>" +
            "SELECT * FROM job_position_level WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (position_code LIKE CONCAT('%', #{keyword}, '%') OR position_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR org_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='orgLevel != null'>" +
            "AND org_level = #{orgLevel} " +
            "</if>" +
            "<if test='orgCode != null and orgCode != \"\"'>" +
            "AND org_code = #{orgCode} " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND position_status = #{status} " +
            "</if>" +
            "ORDER BY org_level, sort_order, create_time DESC " +
            "</script>")
    List<JobPositionLevel> selectList(@Param("keyword") String keyword, @Param("orgLevel") Integer orgLevel,
                                       @Param("orgCode") String orgCode, @Param("status") String status);
    
    /**
     * Select positions by organization ID
     */
    @Select("SELECT * FROM job_position_level WHERE org_id = #{orgId} ORDER BY sort_order, create_time")
    List<JobPositionLevel> selectByOrgId(@Param("orgId") Long orgId);
    
    /**
     * Select positions by organization level
     */
    @Select("SELECT * FROM job_position_level WHERE org_level = #{orgLevel} ORDER BY sort_order, create_time")
    List<JobPositionLevel> selectByOrgLevel(@Param("orgLevel") Integer orgLevel);
    
    /**
     * Select active positions
     */
    @Select("SELECT * FROM job_position_level WHERE position_status = 'ACTIVE' ORDER BY org_level, sort_order")
    List<JobPositionLevel> selectActivePositions();
    
    /**
     * Select vacant positions
     */
    @Select("SELECT * FROM job_position_level WHERE position_status = 'VACANT' ORDER BY org_level, sort_order")
    List<JobPositionLevel> selectVacantPositions();
    
    /**
     * Select core management positions
     */
    @Select("SELECT * FROM job_position_level WHERE is_core_management = true AND position_status != 'ABOLISHED' " +
            "ORDER BY org_level, sort_order")
    List<JobPositionLevel> selectCoreManagementPositions();
    
    /**
     * Select personalized positions
     */
    @Select("SELECT * FROM job_position_level WHERE is_personalized = true ORDER BY org_id, sort_order")
    List<JobPositionLevel> selectPersonalizedPositions();
    
    /**
     * Count positions by organization level
     */
    @Select("SELECT org_level, COUNT(*) as count FROM job_position_level WHERE position_status != 'ABOLISHED' " +
            "GROUP BY org_level")
    List<Map<String, Object>> countByOrgLevel();
    
    /**
     * Count positions by status
     */
    @Select("SELECT position_status, COUNT(*) as count FROM job_position_level GROUP BY position_status")
    List<Map<String, Object>> countByStatus();
    
    /**
     * Count total positions
     */
    @Select("SELECT COUNT(*) FROM job_position_level WHERE position_status != 'ABOLISHED'")
    int countTotal();
    
    /**
     * Count active positions
     */
    @Select("SELECT COUNT(*) FROM job_position_level WHERE position_status = 'ACTIVE'")
    int countActive();
    
    /**
     * Count vacant positions
     */
    @Select("SELECT COUNT(*) FROM job_position_level WHERE position_status = 'VACANT'")
    int countVacant();
    
    /**
     * Count personalized positions
     */
    @Select("SELECT COUNT(*) FROM job_position_level WHERE is_personalized = true")
    int countPersonalized();
    
    /**
     * Check if position code exists
     */
    @Select("SELECT COUNT(*) FROM job_position_level WHERE position_code = #{positionCode}")
    int checkCodeExists(@Param("positionCode") String positionCode);
    
    /**
     * Update incumbent information
     */
    @Update("UPDATE job_position_level SET incumbent_id = #{incumbentId}, incumbent_name = #{incumbentName}, " +
            "position_status = #{positionStatus}, updated_by = #{updatedBy}, update_time = #{updateTime} " +
            "WHERE id = #{id}")
    int updateIncumbent(@Param("id") Long id, @Param("incumbentId") String incumbentId,
                        @Param("incumbentName") String incumbentName, @Param("positionStatus") String positionStatus,
                        @Param("updatedBy") String updatedBy, @Param("updateTime") java.time.LocalDateTime updateTime);
    
    /**
     * Clear incumbent (remove person from position)
     */
    @Update("UPDATE job_position_level SET incumbent_id = NULL, incumbent_name = NULL, position_status = 'VACANT', " +
            "updated_by = #{updatedBy}, update_time = #{updateTime} WHERE id = #{id}")
    int clearIncumbent(@Param("id") Long id, @Param("updatedBy") String updatedBy,
                       @Param("updateTime") java.time.LocalDateTime updateTime);
    
    /**
     * Batch insert positions
     */
    @Insert("<script>" +
            "INSERT INTO job_position_level (position_code, position_name, org_level, org_id, org_code, org_name, " +
            "position_grade, position_status, effective_date, responsibility_desc, sort_order, " +
            "is_core_management, is_in_staffing_control, is_in_command_chain, created_by, create_time) VALUES " +
            "<foreach collection='list' item='pos' separator=','>" +
            "(#{pos.positionCode}, #{pos.positionName}, #{pos.orgLevel}, #{pos.orgId}, #{pos.orgCode}, #{pos.orgName}, " +
            "#{pos.positionGrade}, #{pos.positionStatus}, #{pos.effectiveDate}, #{pos.responsibilityDesc}, #{pos.sortOrder}, " +
            "#{pos.isCoreManagement}, #{pos.isInStaffingControl}, #{pos.isInCommandChain}, #{pos.createdBy}, #{pos.createTime})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("list") List<JobPositionLevel> positions);
}