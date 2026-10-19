package com.example.orgmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orgmgt.entity.CommandChain;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Command Chain Mapper Interface
 * Database operations for command chain management
 */
@Mapper
public interface CommandChainMapper extends BaseMapper<CommandChain> {
    
    /**
     * Select command chain list with filters
     */
    @Select("<script>" +
            "SELECT * FROM command_chain WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            " AND (chain_name LIKE CONCAT('%', #{keyword}, '%') OR chain_code LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "<if test='chainType != null and chainType != \"\"'>" +
            " AND chain_type = #{chainType}" +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            " AND chain_status = #{status}" +
            "</if>" +
            "<if test='business != null and business != \"\"'>" +
            " AND applicable_business = #{business}" +
            "</if>" +
            " ORDER BY create_time DESC" +
            " LIMIT #{pageSize} OFFSET #{offset}" +
            "</script>")
    List<CommandChain> selectList(@Param("keyword") String keyword,
                                  @Param("chainType") String chainType,
                                  @Param("status") String status,
                                  @Param("business") String business,
                                  @Param("page") Integer page,
                                  @Param("pageSize") Integer pageSize,
                                  @Param("offset") Integer offset);
    
    /**
     * Select all command chains
     */
    @Select("SELECT * FROM command_chain ORDER BY create_time DESC")
    List<CommandChain> selectAll();
    
    /**
     * Select command chain by ID
     */
    @Select("SELECT * FROM command_chain WHERE id = #{id}")
    CommandChain selectById(@Param("id") Long id);
    
    /**
     * Select chains by type
     */
    @Select("SELECT * FROM command_chain WHERE chain_type = #{type} ORDER BY create_time DESC")
    List<CommandChain> selectByType(@Param("type") String type);

    /**
     * Select chains by status
     */
    @Select("SELECT * FROM command_chain WHERE chain_status = #{status} ORDER BY create_time DESC")
    List<CommandChain> selectByStatus(@Param("status") String status);
    
    /**
     * Insert command chain
     */
    @Insert("INSERT INTO command_chain (chain_code, chain_name, chain_type, applicable_org_scope, " +
            "applicable_business, start_position, end_position, level_count, effective_start_time, " +
            "effective_end_time, chain_status, allow_skip_level, allow_cross_copy, timeout_warning_hours, " +
            "reject_return_rule, force_chain_flow, related_virtual_org_id, authorization_basis, " +
            "chain_description, remark, created_by, create_time) VALUES " +
            "(#{chainCode}, #{chainName}, #{chainType}, #{applicableOrgScope}, #{applicableBusiness}, " +
            "#{startPosition}, #{endPosition}, #{levelCount}, #{effectiveStartTime}, #{effectiveEndTime}, " +
            "#{chainStatus}, #{allowSkipLevel}, #{allowCrossCopy}, #{timeoutWarningHours}, " +
            "#{rejectReturnRule}, #{forceChainFlow}, #{relatedVirtualOrgId}, #{authorizationBasis}, " +
            "#{chainDescription}, #{remark}, #{createdBy}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CommandChain chain);
    
    /**
     * Update command chain
     */
    @Update("UPDATE command_chain SET " +
            "chain_name = #{chainName}, chain_type = #{chainType}, applicable_org_scope = #{applicableOrgScope}, " +
            "applicable_business = #{applicableBusiness}, effective_start_time = #{effectiveStartTime}, " +
            "effective_end_time = #{effectiveEndTime}, allow_skip_level = #{allowSkipLevel}, " +
            "allow_cross_copy = #{allowCrossCopy}, timeout_warning_hours = #{timeoutWarningHours}, " +
            "reject_return_rule = #{rejectReturnRule}, force_chain_flow = #{forceChainFlow}, " +
            "related_virtual_org_id = #{relatedVirtualOrgId}, authorization_basis = #{authorizationBasis}, " +
            "chain_description = #{chainDescription}, remark = #{remark}, updated_by = #{updatedBy}, " +
            "update_time = #{updateTime} WHERE id = #{id}")
    int update(CommandChain chain);
    
    /**
     * Update chain status
     */
    @Update("UPDATE command_chain SET chain_status = #{status}, update_time = NOW() WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status);
    
    /**
     * Count all chains
     */
    @Select("SELECT COUNT(*) FROM command_chain")
    Integer countAll();
    
    /**
     * Count by status
     */
    @Select("SELECT COUNT(*) FROM command_chain WHERE chain_status = #{status}")
    Integer countByStatus(@Param("status") String status);
    
    /**
     * Select change history
     */
    @Select("SELECT * FROM command_chain_change_history WHERE chain_id = #{id} ORDER BY change_time DESC")
    List<Map<String, Object>> selectChangeHistory(@Param("id") Long id);
    
    /**
     * Insert change history
     */
    @Insert("INSERT INTO command_chain_change_history (change_no, chain_id, change_type, change_desc, " +
            "operator, change_time) VALUES (CONCAT('CCH', UNIX_TIMESTAMP(), FLOOR(RAND()*100000)), " +
            "#{chainId}, #{changeType}, #{changeContent}, #{operator}, #{changeTime})")
    void insertChangeHistory(@Param("chainId") Long chainId,
                            @Param("changeType") String changeType,
                            @Param("changeContent") String changeContent,
                            @Param("operator") String operator,
                            @Param("changeTime") LocalDateTime changeTime);
    
    /**
     * Insert approval request
     */
    @Insert("INSERT INTO command_chain_approval (approval_no, chain_id, change_type, change_content, " +
            "request_time, approval_status) VALUES (CONCAT('CCA', UNIX_TIMESTAMP(), FLOOR(RAND()*100000)), " +
            "#{chainId}, #{changeType}, #{changeContent}, #{requestTime}, 'PENDING')")
    void insertApprovalRequest(@Param("chainId") Long chainId,
                               @Param("changeType") String changeType,
                               @Param("changeContent") String changeContent,
                               @Param("requestTime") LocalDateTime requestTime);
    
    /**
     * Insert batch approval request
     */
    @Insert("INSERT INTO command_chain_batch_approval (chain_ids, change_type, change_content, " +
            "request_time, approval_status) VALUES (#{chainIds}, #{changeType}, #{changeContent}, #{requestTime}, 'PENDING')")
    void insertBatchApprovalRequest(@Param("chainIds") List<Long> chainIds,
                                     @Param("changeType") String changeType,
                                     @Param("changeContent") String changeContent,
                                     @Param("requestTime") LocalDateTime requestTime);
}