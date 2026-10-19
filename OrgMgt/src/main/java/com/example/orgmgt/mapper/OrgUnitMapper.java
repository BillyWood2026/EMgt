package com.example.orgmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orgmgt.entity.OrgUnit;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrgUnitMapper extends BaseMapper<OrgUnit> {

    @Insert("INSERT INTO organization (org_code, org_name, org_level, parent_org_id, parent_org_code, org_status, " +
            "establish_date, org_manager, contact_phone, business_line, location, sort_order, remark, is_core_org, " +
            "is_independent_accounting, is_staffing_controlled, created_by, created_time, updated_by, updated_time, " +
            "deleted, version) VALUES (#{orgCode}, #{orgName}, #{orgLevel}, #{parentOrgId}, #{parentOrgCode}, " +
            "#{orgStatus}, #{establishDate}, #{orgManager}, #{contactPhone}, #{businessLine}, #{location}, " +
            "#{sortOrder}, #{remark}, #{isCoreOrg}, #{isIndependentAccounting}, #{isStaffingControlled}, " +
            "#{createdBy}, #{createdTime}, #{updatedBy}, #{updatedTime}, #{deleted}, #{version})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrgUnit orgUnit);

    @Update("UPDATE organization SET org_code=#{orgCode}, org_name=#{orgName}, org_level=#{orgLevel}, " +
            "parent_org_id=#{parentOrgId}, parent_org_code=#{parentOrgCode}, org_status=#{orgStatus}, " +
            "establish_date=#{establishDate}, org_manager=#{orgManager}, contact_phone=#{contactPhone}, " +
            "business_line=#{businessLine}, location=#{location}, sort_order=#{sortOrder}, remark=#{remark}, " +
            "is_core_org=#{isCoreOrg}, is_independent_accounting=#{isIndependentAccounting}, " +
            "is_staffing_controlled=#{isStaffingControlled}, updated_by=#{updatedBy}, updated_time=#{updatedTime}, " +
            "version=#{version} WHERE id=#{id}")
    int update(OrgUnit orgUnit);

    @Update("UPDATE organization SET deleted=1 WHERE id=#{id}")
    int deleteById(Long id);

    @Update("UPDATE organization SET deleted=1 WHERE id=#{id}")
    int delete(Long id);
    
    @Select("SELECT * FROM organization WHERE org_code=#{orgCode} AND deleted=0")
    OrgUnit selectByCode(String orgCode);
    
    @Select("SELECT * FROM organization WHERE parent_org_id=#{parentId} AND deleted=0 ORDER BY sort_order")
    List<OrgUnit> selectByParentId(Long parentId);
    
    @Select("SELECT * FROM organization WHERE org_level=#{orgLevel} AND deleted=0 ORDER BY sort_order")
    List<OrgUnit> selectByLevel(Integer orgLevel);
    
    @Select("SELECT * FROM organization WHERE deleted=0 ORDER BY sort_order")
    List<OrgUnit> selectAll();
    
    @Select("<script>" +
            "SELECT * FROM organization WHERE deleted=0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (org_code LIKE CONCAT('%', #{keyword}, '%') OR org_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='orgLevel != null and orgLevel != \"\"'>" +
            "AND org_level = #{orgLevel} " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND org_status = #{status} " +
            "</if>" +
            "ORDER BY sort_order " +
            "LIMIT #{pageSize} OFFSET #{offset}" +
            "</script>")
    List<OrgUnit> selectList(@Param("keyword") String keyword, @Param("orgLevel") String orgLevel,
                             @Param("status") String status, @Param("page") Integer page,
                             @Param("pageSize") Integer pageSize, @Param("offset") Integer offset);
    
    @Select("SELECT COUNT(*) FROM organization WHERE deleted=0")
    int countAll();
    
    @Select("SELECT COUNT(*) FROM organization WHERE org_level=#{orgLevel} AND deleted=0")
    int countByLevel(Integer orgLevel);
    
    @Select("SELECT COUNT(*) FROM organization WHERE org_status=#{status} AND deleted=0")
    int countByStatus(String status);
    
    @Select("SELECT id, org_code, org_name, org_level, parent_org_id, parent_org_code, org_status, sort_order " +
            "FROM organization WHERE deleted=0 ORDER BY org_level, sort_order")
    List<Map<String, Object>> selectOrgTree();
    
    @Select("<script>" +
            "SELECT * FROM organization WHERE deleted=0 " +
            "<if test='recursive == true'>" +
            "AND FIND_IN_SET(id, getChildOrgList(#{parentId})) " +
            "</if>" +
            "<if test='recursive == false'>" +
            "AND parent_org_id = #{parentId} " +
            "</if>" +
            "ORDER BY sort_order" +
            "</script>")
    List<OrgUnit> selectChildren(@Param("parentId") Long parentId, @Param("recursive") Boolean recursive);
    
    @Select("SELECT CONCAT(#{parentCode}, LPAD(COALESCE(MAX(CAST(SUBSTRING(org_code, LENGTH(#{parentCode}) + 1) AS UNSIGNED)), 0) + 1, 2, '0')) " +
            "FROM organization WHERE org_code LIKE CONCAT(#{parentCode}, '%') AND LENGTH(org_code) = LENGTH(#{parentCode}) + 2 AND deleted=0")
    String generateOrgCode(@Param("parentCode") String parentCode, @Param("orgLevel") Integer orgLevel);
    
    @Select("SELECT COUNT(*) FROM organization WHERE org_code=#{orgCode} AND deleted=0")
    int checkCodeExists(String orgCode);
    
    @Select("<script>" +
            "SELECT COUNT(*) FROM organization WHERE org_name=#{orgName} AND deleted=0 " +
            "<if test='excludeId != null'>" +
            "AND id != #{excludeId} " +
            "</if>" +
            "</script>")
    int checkNameExists(@Param("orgName") String orgName, @Param("excludeId") Long excludeId);
}
