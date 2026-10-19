package com.example.orgmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orgmgt.entity.VirtualOrgMember;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * Virtual Organization Member Mapper Interface
 * Database operations for virtual organization member management
 */
@Mapper
public interface VirtualOrganizationMemberMapper extends BaseMapper<VirtualOrgMember> {

    /**
     * Select active members by virtual organization ID
     */
    @Select("SELECT * FROM virtual_organization_member WHERE virtual_org_id = #{id} " +
            "AND deleted = 0 AND member_status = 'ACTIVE' ORDER BY join_date")
    List<VirtualOrgMember> selectByVirtualOrgId(@Param("id") Long id);

    /**
     * Select member by ID
     */
    @Select("SELECT * FROM virtual_organization_member WHERE id = #{id} AND deleted = 0")
    VirtualOrgMember selectById(@Param("id") Long id);

    /**
     * Count active members by virtual organization ID
     */
    @Select("SELECT COUNT(*) FROM virtual_organization_member WHERE virtual_org_id = #{id} " +
            "AND deleted = 0 AND member_status = 'ACTIVE'")
    Integer countByVirtualOrgId(@Param("id") Long id);

    /**
     * Select distinct virtual organization IDs for an employee
     */
    @Select("SELECT DISTINCT virtual_org_id FROM virtual_organization_member " +
            "WHERE employee_id = #{employeeId} AND deleted = 0")
    List<Long> selectOrgIdsByEmployee(@Param("employeeId") String employeeId);

    /**
     * Insert member
     */
    @Insert("INSERT INTO virtual_organization_member (virtual_org_id, virtual_org_code, virtual_org_name, " +
            "employee_id, employee_name, original_org_id, original_org_code, original_org_name, " +
            "original_position_code, original_position_name, virtual_role, join_date, member_status, " +
            "remarks, created_by, create_time, deleted, version) VALUES " +
            "(#{virtualOrgId}, #{virtualOrgCode}, #{virtualOrgName}, #{employeeId}, #{employeeName}, " +
            "#{originalOrgId}, #{originalOrgCode}, #{originalOrgName}, #{originalPositionCode}, #{originalPositionName}, " +
            "#{virtualRole}, #{joinDate}, #{memberStatus}, #{remarks}, #{createdBy}, #{createTime}, " +
            "#{deleted}, #{version})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(VirtualOrgMember member);

    /**
     * Update member (used for role change and leave)
     */
    @Update("UPDATE virtual_organization_member SET " +
            "virtual_role = #{virtualRole}, leave_date = #{leaveDate}, member_status = #{memberStatus}, " +
            "remarks = #{remarks} WHERE id = #{id} AND deleted = 0")
    int update(VirtualOrgMember member);

    /**
     * Logical delete member by ID
     */
    @Update("UPDATE virtual_organization_member SET deleted = 1 WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * Logical delete all members by virtual organization ID
     */
    @Update("UPDATE virtual_organization_member SET deleted = 1 WHERE virtual_org_id = #{id}")
    int deleteByVirtualOrgId(@Param("id") Long id);
}
