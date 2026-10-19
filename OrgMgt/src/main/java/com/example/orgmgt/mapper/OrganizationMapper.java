package com.example.orgmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orgmgt.entity.Organization;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrganizationMapper extends BaseMapper<Organization> {

    @Insert("INSERT INTO organization (org_code, org_name, org_level, parent_org_id, parent_org_code, org_status, " +
            "establish_date, org_manager, contact_phone, business_line, location, sort_order, remark, is_core_org, " +
            "is_independent_accounting, is_staffing_controlled, created_by, created_time, updated_by, updated_time, " +
            "deleted, version) VALUES (#{orgCode}, #{orgName}, #{orgLevel}, #{parentOrgId}, #{parentOrgCode}, " +
            "#{orgStatus}, #{establishDate}, #{orgManager}, #{contactPhone}, #{businessLine}, #{location}, " +
            "#{sortOrder}, #{remark}, #{isCoreOrg}, #{isIndependentAccounting}, #{isStaffingControlled}, " +
            "#{createdBy}, #{createdTime}, #{updatedBy}, #{updatedTime}, #{deleted}, #{version})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Organization organization);

    @Update("UPDATE organization SET org_code=#{orgCode}, org_name=#{orgName}, org_level=#{orgLevel}, " +
            "parent_org_id=#{parentOrgId}, parent_org_code=#{parentOrgCode}, org_status=#{orgStatus}, " +
            "establish_date=#{establishDate}, org_manager=#{orgManager}, contact_phone=#{contactPhone}, " +
            "business_line=#{businessLine}, location=#{location}, sort_order=#{sortOrder}, remark=#{remark}, " +
            "is_core_org=#{isCoreOrg}, is_independent_accounting=#{isIndependentAccounting}, " +
            "is_staffing_controlled=#{isStaffingControlled}, updated_by=#{updatedBy}, updated_time=#{updatedTime}, " +
            "version=#{version} WHERE id=#{id}")
    int update(Organization organization);

    @Update("UPDATE organization SET deleted=1 WHERE id=#{id}")
    int deleteById(Long id);
    
    @Select("SELECT * FROM organization WHERE deleted=0")
    List<Organization> findAll();
    
    @Select("SELECT * FROM organization WHERE id=#{id} AND deleted=0")
    Organization findById(Long id);
    
    @Select("SELECT * FROM organization WHERE org_code=#{orgCode} AND deleted=0")
    Organization findByOrgCode(String orgCode);
    
    @Select("SELECT * FROM organization WHERE org_level=#{orgLevel} AND deleted=0 ORDER BY sort_order")
    List<Organization> findByOrgLevel(Integer orgLevel);
    
    @Select("SELECT * FROM organization WHERE parent_org_id=#{parentOrgId} AND deleted=0 ORDER BY sort_order")
    List<Organization> findByParentOrgId(Long parentOrgId);
    
    @Select("SELECT * FROM organization WHERE org_status=#{orgStatus} AND deleted=0")
    List<Organization> findByOrgStatus(String orgStatus);
    
    @Select("SELECT * FROM organization WHERE org_name LIKE CONCAT('%', #{orgName}, '%') AND deleted=0")
    List<Organization> findByOrgNameLike(String orgName);
    
    @Update("UPDATE organization SET org_status=#{status} WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    @Select("SELECT * FROM organization WHERE business_line=#{businessLine} AND deleted=0")
    List<Organization> findByBusinessLine(String businessLine);
    
    @Select("SELECT * FROM organization WHERE is_core_org=#{isCoreOrg} AND deleted=0")
    List<Organization> findByIsCoreOrg(Integer isCoreOrg);
}
