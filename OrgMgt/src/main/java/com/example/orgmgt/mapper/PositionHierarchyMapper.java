package com.example.orgmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orgmgt.entity.PositionHierarchy;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PositionHierarchyMapper extends BaseMapper<PositionHierarchy> {

    @Insert("INSERT INTO position_hierarchy (hierarchy_code, hierarchy_name, org_level, org_id, org_code, org_name, " +
            "hierarchy_grade, hierarchy_status, incumbents, effective_date, expire_date, duty_description, sort_order, " +
            "is_core_management, is_concurrent, is_seconded, remark, created_by, created_time, updated_by, updated_time, " +
            "deleted, version) VALUES (#{hierarchyCode}, #{hierarchyName}, #{orgLevel}, #{orgId}, #{orgCode}, #{orgName}, " +
            "#{hierarchyGrade}, #{hierarchyStatus}, #{incumbents}, #{effectiveDate}, #{expireDate}, #{dutyDescription}, " +
            "#{sortOrder}, #{isCoreManagement}, #{isConcurrent}, #{isSeconded}, #{remark}, #{createdBy}, #{createdTime}, " +
            "#{updatedBy}, #{updatedTime}, #{deleted}, #{version})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PositionHierarchy hierarchy);

    @Update("UPDATE position_hierarchy SET hierarchy_code=#{hierarchyCode}, hierarchy_name=#{hierarchyName}, " +
            "org_level=#{orgLevel}, org_id=#{orgId}, org_code=#{orgCode}, org_name=#{orgName}, " +
            "hierarchy_grade=#{hierarchyGrade}, hierarchy_status=#{hierarchyStatus}, incumbents=#{incumbents}, " +
            "effective_date=#{effectiveDate}, expire_date=#{expireDate}, duty_description=#{dutyDescription}, " +
            "sort_order=#{sortOrder}, is_core_management=#{isCoreManagement}, is_concurrent=#{isConcurrent}, " +
            "is_seconded=#{isSeconded}, remark=#{remark}, updated_by=#{updatedBy}, updated_time=#{updatedTime}, " +
            "version=#{version} WHERE id=#{id}")
    int update(PositionHierarchy hierarchy);

    @Update("UPDATE position_hierarchy SET deleted=1 WHERE id=#{id}")
    int deleteById(Long id);
    
    @Select("SELECT * FROM position_hierarchy WHERE deleted=0")
    List<PositionHierarchy> findAll();
    
    @Select("SELECT * FROM position_hierarchy WHERE id=#{id} AND deleted=0")
    PositionHierarchy findById(Long id);
    
    @Select("SELECT * FROM position_hierarchy WHERE hierarchy_code=#{hierarchyCode} AND deleted=0")
    PositionHierarchy findByHierarchyCode(String hierarchyCode);
    
    @Select("SELECT * FROM position_hierarchy WHERE org_level=#{orgLevel} AND deleted=0 ORDER BY sort_order")
    List<PositionHierarchy> findByOrgLevel(Integer orgLevel);
    
    @Select("SELECT * FROM position_hierarchy WHERE org_id=#{orgId} AND deleted=0 ORDER BY sort_order")
    List<PositionHierarchy> findByOrgId(Long orgId);
    
    @Select("SELECT * FROM position_hierarchy WHERE org_code=#{orgCode} AND deleted=0 ORDER BY sort_order")
    List<PositionHierarchy> findByOrgCode(String orgCode);
    
    @Select("SELECT * FROM position_hierarchy WHERE hierarchy_status=#{hierarchyStatus} AND deleted=0")
    List<PositionHierarchy> findByHierarchyStatus(String hierarchyStatus);
    
    @Update("UPDATE position_hierarchy SET hierarchy_status=#{status} WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    @Select("SELECT * FROM position_hierarchy WHERE incumbents LIKE CONCAT('%', #{incumbents}, '%') AND deleted=0")
    List<PositionHierarchy> findByIncumbents(String incumbents);
    
    @Select("SELECT * FROM position_hierarchy WHERE is_core_management=#{isCoreManagement} AND deleted=0")
    List<PositionHierarchy> findByIsCoreManagement(Integer isCoreManagement);
}
