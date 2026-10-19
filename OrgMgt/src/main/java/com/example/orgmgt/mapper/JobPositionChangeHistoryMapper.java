package com.example.orgmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orgmgt.entity.JobPositionChangeHistory;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * Job Position Change History Mapper
 * Data access interface for immutable change history tracking
 * Permanent record for state-owned enterprise compliance and audit
 */
@Mapper
public interface JobPositionChangeHistoryMapper extends BaseMapper<JobPositionChangeHistory> {
    
    /**
     * Insert change history record
     * Immutable once created, cannot be updated or deleted
     */
    @Insert("INSERT INTO job_position_change_history (change_no, position_id, position_code, position_name, " +
            "change_type, before_data, after_data, change_desc, initiator_id, initiator_name, change_time, " +
            "approval_nodes, approval_result, approval_time, operator, ip_address, version_no, " +
            "attachment_path, attachment_url, remarks, create_time) " +
            "VALUES (#{changeNo}, #{positionId}, #{positionCode}, #{positionName}, #{changeType}, " +
            "#{beforeData}, #{afterData}, #{changeDesc}, #{initiatorId}, #{initiatorName}, #{changeTime}, " +
            "#{approvalNodes}, #{approvalResult}, #{approvalTime}, #{operator}, #{ipAddress}, #{versionNo}, " +
            "#{attachmentPath}, #{attachmentUrl}, #{remarks}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(JobPositionChangeHistory history);
    
    /**
     * Select by ID
     */
    @Select("SELECT * FROM job_position_change_history WHERE id = #{id}")
    JobPositionChangeHistory selectById(@Param("id") Long id);
    
    /**
     * Select by change number
     */
    @Select("SELECT * FROM job_position_change_history WHERE change_no = #{changeNo}")
    JobPositionChangeHistory selectByChangeNo(@Param("changeNo") String changeNo);
    
    /**
     * Select all history with filters
     */
    @Select("<script>" +
            "SELECT * FROM job_position_change_history WHERE 1=1 " +
            "<if test='positionCode != null and positionCode != \"\"'>" +
            "AND position_code = #{positionCode} " +
            "</if>" +
            "<if test='changeType != null and changeType != \"\"'>" +
            "AND change_type = #{changeType} " +
            "</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (change_no LIKE CONCAT('%', #{keyword}, '%') OR position_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "ORDER BY change_time DESC " +
            "</script>")
    List<JobPositionChangeHistory> selectList(@Param("positionCode") String positionCode,
                                               @Param("changeType") String changeType,
                                               @Param("keyword") String keyword);
    
    /**
     * Select history by position ID
     */
    @Select("SELECT * FROM job_position_change_history WHERE position_id = #{positionId} ORDER BY change_time DESC")
    List<JobPositionChangeHistory> selectByPositionId(@Param("positionId") Long positionId);
    
    /**
     * Select history by position code
     */
    @Select("SELECT * FROM job_position_change_history WHERE position_code = #{positionCode} ORDER BY change_time DESC")
    List<JobPositionChangeHistory> selectByPositionCode(@Param("positionCode") String positionCode);
    
    /**
     * Select history by change type
     */
    @Select("SELECT * FROM job_position_change_history WHERE change_type = #{changeType} ORDER BY change_time DESC")
    List<JobPositionChangeHistory> selectByChangeType(@Param("changeType") String changeType);
    
    /**
     * Select history by initiator
     */
    @Select("SELECT * FROM job_position_change_history WHERE initiator_id = #{initiatorId} ORDER BY change_time DESC")
    List<JobPositionChangeHistory> selectByInitiator(@Param("initiatorId") String initiatorId);
    
    /**
     * Select history by approval result
     */
    @Select("SELECT * FROM job_position_change_history WHERE approval_result = #{approvalResult} ORDER BY change_time DESC")
    List<JobPositionChangeHistory> selectByApprovalResult(@Param("approvalResult") String approvalResult);
    
    /**
     * Select latest version for position
     */
    @Select("SELECT * FROM job_position_change_history WHERE position_code = #{positionCode} " +
            "ORDER BY create_time DESC LIMIT 1")
    JobPositionChangeHistory selectLatestVersion(@Param("positionCode") String positionCode);
    
    /**
     * Select version history for position
     */
    @Select("SELECT * FROM job_position_change_history WHERE position_code = #{positionCode} " +
            "ORDER BY create_time DESC")
    List<JobPositionChangeHistory> selectVersionHistory(@Param("positionCode") String positionCode);
    
    /**
     * Select two versions for comparison
     */
    @Select("SELECT * FROM job_position_change_history WHERE id IN (#{id1}, #{id2}) ORDER BY create_time DESC")
    List<JobPositionChangeHistory> selectForComparison(@Param("id1") Long id1, @Param("id2") Long id2);
    
    /**
     * Count total history records
     */
    @Select("SELECT COUNT(*) FROM job_position_change_history")
    int countTotal();
    
    /**
     * Count history by position
     */
    @Select("SELECT COUNT(*) FROM job_position_change_history WHERE position_code = #{positionCode}")
    int countByPositionCode(@Param("positionCode") String positionCode);
    
    /**
     * Count history by change type
     */
    @Select("SELECT change_type, COUNT(*) as count FROM job_position_change_history GROUP BY change_type")
    List<java.util.Map<String, Object>> countByChangeType();
    
    /**
     * Count history by approval result
     */
    @Select("SELECT approval_result, COUNT(*) as count FROM job_position_change_history GROUP BY approval_result")
    List<java.util.Map<String, Object>> countByApprovalResult();
    
    /**
     * Generate next change number
     */
    @Select("SELECT CONCAT('JCH-', DATE_FORMAT(NOW(), '%Y%m'), LPAD(COALESCE(MAX(SUBSTRING(change_no, 12)), 0) + 1, 3, '0')) " +
            "FROM job_position_change_history WHERE change_no LIKE CONCAT('JCH-', DATE_FORMAT(NOW(), '%Y%m'), '%')")
    String generateNextChangeNo();
    
    /**
     * Generate next version number for position
     */
    @Select("SELECT CONCAT('V', COALESCE(MAX(CAST(SUBSTRING(version_no, 2) AS DECIMAL)), 0) + 1) " +
            "FROM job_position_change_history WHERE position_code = #{positionCode}")
    String generateNextVersionNo(@Param("positionCode") String positionCode);
    
    /**
     * Select history within date range
     */
    @Select("SELECT * FROM job_position_change_history WHERE change_time BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY change_time DESC")
    List<JobPositionChangeHistory> selectByDateRange(@Param("startDate") java.time.LocalDateTime startDate,
                                                      @Param("endDate") java.time.LocalDateTime endDate);
    
    /**
     * Select pending approvals
     */
    @Select("SELECT * FROM job_position_change_history WHERE approval_result = 'PENDING' ORDER BY change_time")
    List<JobPositionChangeHistory> selectPendingApprovals();
    
    /**
     * Update approval result (only for pending records)
     */
    @Update("UPDATE job_position_change_history SET approval_result = #{approvalResult}, " +
            "approval_time = #{approvalTime} WHERE id = #{id} AND approval_result = 'PENDING'")
    int updateApprovalResult(@Param("id") Long id, @Param("approvalResult") String approvalResult,
                             @Param("approvalTime") java.time.LocalDateTime approvalTime);
}