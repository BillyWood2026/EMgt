package com.example.orgmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orgmgt.entity.VirtualOrganization;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Virtual Organization Mapper Interface
 * Database operations for virtual organization management
 */
@Mapper
public interface VirtualOrganizationMapper extends BaseMapper<VirtualOrganization> {

    /**
     * Select virtual organization list with filters
     */
    @Select("SELECT * FROM virtual_organization WHERE deleted = 0 AND " +
            "(#{keyword} IS NULL OR virtual_org_name LIKE CONCAT('%', #{keyword}, '%') OR virtual_org_code LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{orgType} IS NULL OR org_type = #{orgType}) " +
            "AND (#{status} IS NULL OR org_status = #{status}) " +
            "ORDER BY create_time DESC " +
            "LIMIT #{pageSize} OFFSET #{offset}")
    List<VirtualOrganization> selectList(@Param("keyword") String keyword,
                                         @Param("orgType") String orgType,
                                         @Param("status") String status,
                                         @Param("offset") Integer offset,
                                         @Param("pageSize") Integer pageSize);

    /**
     * Select all virtual organizations
     */
    @Select("SELECT * FROM virtual_organization WHERE deleted = 0 ORDER BY create_time DESC")
    List<VirtualOrganization> selectAll();

    /**
     * Select virtual organization by ID
     */
    @Select("SELECT * FROM virtual_organization WHERE id = #{id} AND deleted = 0")
    VirtualOrganization selectById(@Param("id") Long id);

    /**
     * Insert virtual organization
     */
    @Insert("INSERT INTO virtual_organization (virtual_org_code, virtual_org_name, org_type, leader_id, leader_name, " +
            "lead_org_id, lead_org_code, lead_org_name, participating_orgs, effective_date, expire_date, " +
            "org_status, member_count, work_objective, remarks, created_by, create_time, deleted, version) VALUES " +
            "(#{virtualOrgCode}, #{virtualOrgName}, #{orgType}, #{leaderId}, #{leaderName}, " +
            "#{leadOrgId}, #{leadOrgCode}, #{leadOrgName}, #{participatingOrgs}, #{effectiveDate}, #{expireDate}, " +
            "#{orgStatus}, #{memberCount}, #{workObjective}, #{remarks}, #{createdBy}, #{createTime}, " +
            "#{deleted}, #{version})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(VirtualOrganization org);

    /**
     * Update virtual organization
     */
    @Update("UPDATE virtual_organization SET " +
            "virtual_org_name = #{virtualOrgName}, org_type = #{orgType}, " +
            "leader_id = #{leaderId}, leader_name = #{leaderName}, " +
            "lead_org_id = #{leadOrgId}, lead_org_code = #{leadOrgCode}, lead_org_name = #{leadOrgName}, " +
            "participating_orgs = #{participatingOrgs}, effective_date = #{effectiveDate}, expire_date = #{expireDate}, " +
            "org_status = #{orgStatus}, member_count = #{memberCount}, work_objective = #{workObjective}, remarks = #{remarks}, " +
            "updated_by = #{updatedBy}, update_time = #{updateTime} WHERE id = #{id}")
    int update(VirtualOrganization org);

    /**
     * Update organization status
     */
    @Update("UPDATE virtual_organization SET org_status = #{status}, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * Update member count
     */
    @Update("UPDATE virtual_organization SET member_count = #{count}, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    void updateMemberCount(@Param("id") Long id, @Param("count") Integer count);

    /**
     * Count by status
     */
    @Select("SELECT COUNT(*) FROM virtual_organization WHERE org_status = #{status} AND deleted = 0")
    Integer countByStatus(@Param("status") String status);

    /**
     * Count by expiry range
     */
    @Select("SELECT COUNT(*) FROM virtual_organization WHERE org_status = 'ACTIVE' AND deleted = 0 " +
            "AND expire_date BETWEEN #{start} AND #{end}")
    Integer countByExpiryRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Select change history (archived in virtual_org_history)
     */
    @Select("SELECT id, virtual_org_id AS virtualOrgId, change_type AS changeType, change_desc AS changeContent, " +
            "initiator_name AS operator, change_time AS changeTime " +
            "FROM virtual_org_history WHERE virtual_org_id = #{id} ORDER BY change_time DESC")
    List<Map<String, Object>> selectChangeHistory(@Param("id") Long id);

    /**
     * Insert change history into the immutable history table
     */
    @Insert("INSERT INTO virtual_org_history (change_no, virtual_org_id, change_type, change_desc, " +
            "initiator_id, initiator_name, change_time, approval_result, create_time) VALUES " +
            "(CONCAT('VCHG-A-', #{orgId}, '-', UNIX_TIMESTAMP(NOW(3))), #{orgId}, #{changeType}, #{changeContent}, " +
            "#{operator}, #{operator}, #{changeTime}, 'APPROVED', NOW())")
    void insertChangeHistory(@Param("orgId") Long orgId,
                            @Param("changeType") String changeType,
                            @Param("changeContent") String changeContent,
                            @Param("operator") String operator,
                            @Param("changeTime") LocalDateTime changeTime);

    /**
     * Insert approval request (recorded as history in the local deployment)
     */
    @Insert("INSERT INTO virtual_org_history (change_no, virtual_org_id, change_type, change_desc, " +
            "change_time, approval_result, create_time) VALUES " +
            "(CONCAT('VCHG-P-', #{orgId}, '-', UNIX_TIMESTAMP(NOW(3))), #{orgId}, #{changeType}, #{changeContent}, " +
            "#{requestTime}, 'PENDING', NOW())")
    void insertApprovalRequest(@Param("orgId") Long orgId,
                               @Param("changeType") String changeType,
                               @Param("changeContent") String changeContent,
                               @Param("requestTime") LocalDateTime requestTime);

    /**
     * Select active virtual organizations expiring within days
     */
    @Select("SELECT * FROM virtual_organization WHERE org_status = 'ACTIVE' AND deleted = 0 " +
            "AND expire_date <= DATE_ADD(NOW(), INTERVAL #{days} DAY) " +
            "AND expire_date >= NOW()")
    List<VirtualOrganization> selectExpiringOrgs(@Param("days") int days);

    /**
     * Select active virtual organizations that have already expired
     */
    @Select("SELECT * FROM virtual_organization WHERE org_status = 'ACTIVE' AND deleted = 0 " +
            "AND expire_date < NOW()")
    List<VirtualOrganization> selectExpiredOrgs();
}
