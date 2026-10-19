package com.example.orgmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orgmgt.entity.PositionHierarchyHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PositionHierarchyHistoryMapper extends BaseMapper<PositionHierarchyHistory> {

    @Insert("INSERT INTO position_hierarchy_history (change_no, hierarchy_code, hierarchy_name, org_level, " +
            "change_type, before_content, after_content, initiator, approval_nodes, approval_time, attachments, " +
            "operation_record) VALUES (#{changeNo}, #{hierarchyCode}, #{hierarchyName}, #{orgLevel}, " +
            "#{changeType}, #{beforeContent}, #{afterContent}, #{initiator}, #{approvalNodes}, #{approvalTime}, " +
            "#{attachments}, #{operationRecord})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PositionHierarchyHistory history);
    
    @Select("SELECT * FROM position_hierarchy_history ORDER BY id DESC")
    List<PositionHierarchyHistory> findAll();
    
    @Select("SELECT * FROM position_hierarchy_history WHERE id=#{id}")
    PositionHierarchyHistory findById(Long id);
    
    @Select("SELECT * FROM position_hierarchy_history WHERE change_no=#{changeNo}")
    PositionHierarchyHistory findByChangeNo(String changeNo);
    
    @Select("SELECT * FROM position_hierarchy_history WHERE hierarchy_code=#{hierarchyCode} ORDER BY id DESC")
    List<PositionHierarchyHistory> findByHierarchyCode(String hierarchyCode);
    
    @Select("SELECT * FROM position_hierarchy_history WHERE org_level=#{orgLevel} ORDER BY id DESC")
    List<PositionHierarchyHistory> findByOrgLevel(Integer orgLevel);
    
    @Select("SELECT * FROM position_hierarchy_history WHERE change_type=#{changeType} ORDER BY id DESC")
    List<PositionHierarchyHistory> findByChangeType(String changeType);
}
