package com.example.orgmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orgmgt.entity.CommandChainNode;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * Command Chain Node Mapper Interface
 * Database operations for command chain node management
 */
@Mapper
public interface CommandChainNodeMapper extends BaseMapper<CommandChainNode> {
    
    /**
     * Select nodes by chain ID
     */
    @Select("SELECT * FROM command_chain_node WHERE chain_id = #{id} AND node_status = 'ACTIVE' ORDER BY node_order")
    List<CommandChainNode> selectByChainId(@Param("id") Long id);
    
    /**
     * Select node by ID
     */
    @Select("SELECT * FROM command_chain_node WHERE id = #{id}")
    CommandChainNode selectById(@Param("id") Long id);
    
    /**
     * Count nodes by chain ID
     */
    @Select("SELECT COUNT(*) FROM command_chain_node WHERE chain_id = #{id} AND node_status = 'ACTIVE'")
    Integer countByChainId(@Param("id") Long id);
    
    /**
     * Insert node
     */
    @Insert("INSERT INTO command_chain_node (chain_id, node_order, node_type, select_by, target_name, " +
            "target_id, node_title, responsible_person, permissions, timeout_hours, timeout_action, " +
            "node_status, remark, created_by, create_time) VALUES " +
            "(#{chainId}, #{nodeOrder}, #{nodeType}, #{selectBy}, #{targetName}, #{targetId}, #{nodeTitle}, " +
            "#{responsiblePerson}, #{permissions}, #{timeoutHours}, #{timeoutAction}, #{nodeStatus}, " +
            "#{remark}, #{createdBy}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CommandChainNode node);
    
    /**
     * Update node
     */
    @Update("UPDATE command_chain_node SET " +
            "node_order = #{nodeOrder}, node_type = #{nodeType}, select_by = #{selectBy}, " +
            "target_name = #{targetName}, target_id = #{targetId}, node_title = #{nodeTitle}, " +
            "responsible_person = #{responsiblePerson}, permissions = #{permissions}, " +
            "timeout_hours = #{timeoutHours}, timeout_action = #{timeoutAction}, node_status = #{nodeStatus}, " +
            "remark = #{remark}, updated_by = #{updatedBy}, update_time = #{updateTime} WHERE id = #{id}")
    int update(CommandChainNode node);
    
    /**
     * Delete node by ID
     */
    @Delete("DELETE FROM command_chain_node WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
    
    /**
     * Delete all nodes by chain ID
     */
    @Delete("DELETE FROM command_chain_node WHERE chain_id = #{id}")
    int deleteByChainId(@Param("id") Long id);
}