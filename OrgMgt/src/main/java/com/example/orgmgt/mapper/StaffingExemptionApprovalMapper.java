package com.example.orgmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orgmgt.entity.StaffingExemptionApproval;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StaffingExemptionApprovalMapper extends BaseMapper<StaffingExemptionApproval> {

    @Insert("INSERT INTO staffing_exemption_approval (exemption_no, org_id, org_code, org_name, position_id, " +
            "position_code, position_name, exemption_type, violation_type, standard_value, current_value, " +
            "exemption_value, exemption_reason, business_justification, effective_date, expire_date, " +
            "monitoring_required, monitoring_frequency, approval_status, current_node, initiator_id, " +
            "initiator_name, submit_time, approval_time, approver_id, approver_name, reject_reason, " +
            "approval_history, attachment_path, compliance_status, compliance_notes, created_by, create_time, " +
            "updated_by, update_time, deleted, version) VALUES (#{exemptionNo}, #{orgId}, #{orgCode}, #{orgName}, " +
            "#{positionId}, #{positionCode}, #{positionName}, #{exemptionType}, #{violationType}, #{standardValue}, " +
            "#{currentValue}, #{exemptionValue}, #{exemptionReason}, #{businessJustification}, #{effectiveDate}, " +
            "#{expireDate}, #{monitoringRequired}, #{monitoringFrequency}, #{approvalStatus}, #{currentNode}, " +
            "#{initiatorId}, #{initiatorName}, #{submitTime}, #{approvalTime}, #{approverId}, #{approverName}, " +
            "#{rejectReason}, #{approvalHistory}, #{attachmentPath}, #{complianceStatus}, #{complianceNotes}, " +
            "#{createdBy}, #{createTime}, #{updatedBy}, #{updateTime}, #{deleted}, #{version})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StaffingExemptionApproval exemption);
    
    @Select("SELECT * FROM staffing_exemption_approval WHERE exemption_no=#{exemptionNo} AND deleted=0")
    StaffingExemptionApproval findByExemptionNo(String exemptionNo);
    
    @Select("SELECT * FROM staffing_exemption_approval WHERE org_id=#{orgId} AND deleted=0")
    List<StaffingExemptionApproval> findByOrgId(Long orgId);
    
    @Select("SELECT * FROM staffing_exemption_approval WHERE position_id=#{positionId} AND deleted=0")
    List<StaffingExemptionApproval> findByPositionId(Long positionId);
    
    @Select("SELECT * FROM staffing_exemption_approval WHERE approval_status='APPROVED' AND expire_date > NOW() AND deleted=0")
    List<StaffingExemptionApproval> findActiveExemptions();
    
    @Select("SELECT * FROM staffing_exemption_approval WHERE approval_status='APPROVED' AND expire_date < NOW() AND deleted=0")
    List<StaffingExemptionApproval> findExpiredExemptions();
    
    @Select("SELECT * FROM staffing_exemption_approval WHERE approval_status=#{approvalStatus} AND deleted=0")
    List<StaffingExemptionApproval> findByApprovalStatus(String approvalStatus);
    
    @Select("SELECT * FROM staffing_exemption_approval WHERE exemption_type=#{exemptionType} AND deleted=0")
    List<StaffingExemptionApproval> findByExemptionType(String exemptionType);
    
    @Select("SELECT * FROM staffing_exemption_approval WHERE compliance_status=#{complianceStatus} AND deleted=0")
    List<StaffingExemptionApproval> findByComplianceStatus(String complianceStatus);
    
    @Update("UPDATE staffing_exemption_approval SET approval_status=#{status} WHERE id=#{id}")
    int updateApprovalStatus(@Param("id") Long id, @Param("status") String status);
    
    @Update("UPDATE staffing_exemption_approval SET compliance_status=#{status} WHERE id=#{id}")
    int updateComplianceStatus(@Param("id") Long id, @Param("status") String status);
    
    @Select("SELECT COUNT(*) FROM staffing_exemption_approval WHERE org_id=#{orgId} AND approval_status='APPROVED' AND expire_date > NOW() AND deleted=0")
    int countActiveExemptionsByOrg(Long orgId);
}
