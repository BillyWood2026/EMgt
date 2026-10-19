package com.example.orgmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orgmgt.entity.VirtualOrgHistory;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * Virtual Organization History Mapper
 * Data access interface for virtual organization lifecycle tracking
 * Permanent record for audit and compliance
 */
@Mapper
public interface VirtualOrgHistoryMapper extends BaseMapper<VirtualOrgHistory> {
    
    /**
     * Insert virtual org history record
     * Immutable once created, cannot be updated or deleted
     */
    @Insert("INSERT INTO virtual_org_history (change_no, virtual_org_id, virtual_org_code, virtual_org_name, " +
            "change_type, before_data, after_data, change_desc, initiator_id, initiator_name, change_time, " +
            "approval_nodes, approval_result, approval_time, attachment_path, remarks, create_time) " +
            "VALUES (#{changeNo}, #{virtualOrgId}, #{virtualOrgCode}, #{virtualOrgName}, #{changeType}, " +
            "#{beforeData}, #{afterData}, #{changeDesc}, #{initiatorId}, #{initiatorName}, #{changeTime}, " +
            "#{approvalNodes}, #{approvalResult}, #{approvalTime}, #{attachmentPath}, #{remarks}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(VirtualOrgHistory history);
    
    /**
     * Select by ID
     */
    @Select("SELECT * FROM virtual_org_history WHERE id = #{id}")
    VirtualOrgHistory selectById(@Param("id") Long id);
    
    /**
     * Select by change number
     */
    @Select("SELECT * FROM virtual_org_history WHERE change_no = #{changeNo}")
    VirtualOrgHistory selectByChangeNo(@Param("changeNo") String changeNo);
    
    /**
     * Select all history with filters
     */
    @Select("<script>" +
            "SELECT * FROM virtual_org_history WHERE 1=1 " +
            "<if test='virtualOrgCode != null and virtualOrgCode != \"\"'>" +
            "AND virtual_org_code = #{virtualOrgCode} " +
            "</if>" +
            "<if test='changeType != null and changeType != \"\"'>" +
            "AND change_type = #{changeType} " +
            "</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (change_no LIKE CONCAT('%', #{keyword}, '%') OR virtual_org_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "ORDER BY change_time DESC " +
            "</script>")
    List<VirtualOrgHistory> selectList(@Param("virtualOrgCode") String virtualOrgCode,
                                        @Param("changeType") String changeType,
                                        @Param("keyword") String keyword);
    
    /**
     * Select history by virtual org ID
     */
    @Select("SELECT * FROM virtual_org_history WHERE virtual_org_id = #{virtualOrgId} ORDER BY change_time DESC")
    List<VirtualOrgHistory> selectByVirtualOrgId(@Param("virtualOrgId") Long virtualOrgId);
    
    /**
     * Select history by virtual org code
     */
    @Select("SELECT * FROM virtual_org_history WHERE virtual_org_code = #{virtualOrgCode} ORDER BY change_time DESC")
    List<VirtualOrgHistory> selectByVirtualOrgCode(@Param("virtualOrgCode") String virtualOrgCode);
    
    /**
     * Select history by change type
     */
    @Select("SELECT * FROM virtual_org_history WHERE change_type = #{changeType} ORDER BY change_time DESC")
    List<VirtualOrgHistory> selectByChangeType(@Param("changeType") String changeType);
    
    /**
     * Select history by initiator
     */
    @Select("SELECT * FROM virtual_org_history WHERE initiator_id = #{initiatorId} ORDER BY change_time DESC")
    List<VirtualOrgHistory> selectByInitiator(@Param("initiatorId") String initiatorId);
    
    /**
     * Select history by approval result
     */
    @Select("SELECT * FROM virtual_org_history WHERE approval_result = #{approvalResult} ORDER BY change_time DESC")
    List<VirtualOrgHistory> selectByApprovalResult(@Param("approvalResult") String approvalResult);
    
    /**
     * Select creation history
     */
    @Select("SELECT * FROM virtual_org_history WHERE change_type = 'CREATE' ORDER BY change_time DESC")
    List<VirtualOrgHistory> selectCreationHistory();
    
    /**
     * Select member change history
     */
    @Select("SELECT * FROM virtual_org_history WHERE change_type = 'MEMBER_CHANGE' ORDER BY change_time DESC")
    List<VirtualOrgHistory> selectMemberChangeHistory();
    
    /**
     * Select leader change history
     */
    @Select("SELECT * FROM virtual_org_history WHERE change_type = 'LEADER_CHANGE' ORDER BY change_time DESC")
    List<VirtualOrgHistory> selectLeaderChangeHistory();
    
    /**
     * Select extension history
     */
    @Select("SELECT * FROM virtual_org_history WHERE change_type = 'EXTEND' ORDER BY change_time DESC")
    List<VirtualOrgHistory> selectExtensionHistory();
    
    /**
     * Select disband history
     */
    @Select("SELECT * FROM virtual_org_history WHERE change_type = 'DISBAND' ORDER BY change_time DESC")
    List<VirtualOrgHistory> selectDisbandHistory();
    
    /**
     * Count total history records
     */
    @Select("SELECT COUNT(*) FROM virtual_org_history")
    int countTotal();
    
    /**
     * Count history by virtual org
     */
    @Select("SELECT COUNT(*) FROM virtual_org_history WHERE virtual_org_code = #{virtualOrgCode}")
    int countByVirtualOrgCode(@Param("virtualOrgCode") String virtualOrgCode);
    
    /**
     * Count history by change type
     */
    @Select("SELECT change_type, COUNT(*) as count FROM virtual_org_history GROUP BY change_type")
    List<java.util.Map<String, Object>> countByChangeType();
    
    /**
     * Count history by approval result
     */
    @Select("SELECT approval_result, COUNT(*) as count FROM virtual_org_history GROUP BY approval_result")
    List<java.util.Map<String, Object>> countByApprovalResult();
    
    /**
     * Generate next change number
     */
    @Select("SELECT CONCAT('VCH-', DATE_FORMAT(NOW(), '%Y%m'), LPAD(COALESCE(MAX(SUBSTRING(change_no, 12)), 0) + 1, 3, '0')) " +
            "FROM virtual_org_history WHERE change_no LIKE CONCAT('VCH-', DATE_FORMAT(NOW(), '%Y%m'), '%')")
    String generateNextChangeNo();
    
    /**
     * Select history within date range
     */
    @Select("SELECT * FROM virtual_org_history WHERE change_time BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY change_time DESC")
    List<VirtualOrgHistory> selectByDateRange(@Param("startDate") java.time.LocalDateTime startDate,
                                               @Param("endDate") java.time.LocalDateTime endDate);
    
    /**
     * Select pending approvals
     */
    @Select("SELECT * FROM virtual_org_history WHERE approval_result = 'PENDING' ORDER BY change_time")
    List<VirtualOrgHistory> selectPendingApprovals();
    
    /**
     * Update approval result (only for pending records)
     */
    @Update("UPDATE virtual_org_history SET approval_result = #{approvalResult}, " +
            "approval_time = #{approvalTime} WHERE id = #{id} AND approval_result = 'PENDING'")
    int updateApprovalResult(@Param("id") Long id, @Param("approvalResult") String approvalResult,
                             @Param("approvalTime") java.time.LocalDateTime approvalTime);
    
    /**
     * Select history for specific virtual org with full lifecycle
     */
    @Select("SELECT * FROM virtual_org_history WHERE virtual_org_id = #{virtualOrgId} " +
            "ORDER BY change_time ASC")
    List<VirtualOrgHistory> selectLifecycleHistory(@Param("virtualOrgId") Long virtualOrgId);
    
    /**
     * Select latest history for virtual org
     */
    @Select("SELECT * FROM virtual_org_history WHERE virtual_org_code = #{virtualOrgCode} " +
            "ORDER BY change_time DESC LIMIT 1")
    VirtualOrgHistory selectLatestHistory(@Param("virtualOrgCode") String virtualOrgCode);
}