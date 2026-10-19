package com.example.orgmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orgmgt.entity.OrgApproval;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrgApprovalMapper extends BaseMapper<OrgApproval> {

    @Insert("INSERT INTO org_approval (approval_no, business_id, business_type, approval_type, current_node, " +
            "current_approver, approval_status, reject_reason, cc_users, sign_users, submit_time, approval_time, " +
            "operation_log, ip_address, attachments, created_by, create_time, updated_by, update_time, deleted, " +
            "version) VALUES (#{approvalNo}, #{businessId}, #{businessType}, #{approvalType}, #{currentNode}, " +
            "#{currentApprover}, #{approvalStatus}, #{rejectReason}, #{ccUsers}, #{signUsers}, #{submitTime}, " +
            "#{approvalTime}, #{operationLog}, #{ipAddress}, #{attachments}, #{createdBy}, #{createTime}, " +
            "#{updatedBy}, #{updateTime}, #{deleted}, #{version})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrgApproval approval);

    @Update("UPDATE org_approval SET approval_no=#{approvalNo}, business_id=#{businessId}, " +
            "business_type=#{businessType}, approval_type=#{approvalType}, current_node=#{currentNode}, " +
            "current_approver=#{currentApprover}, approval_status=#{approvalStatus}, reject_reason=#{rejectReason}, " +
            "cc_users=#{ccUsers}, sign_users=#{signUsers}, submit_time=#{submitTime}, approval_time=#{approvalTime}, " +
            "operation_log=#{operationLog}, ip_address=#{ipAddress}, attachments=#{attachments}, " +
            "updated_by=#{updatedBy}, update_time=#{updateTime}, version=#{version} WHERE id=#{id}")
    int update(OrgApproval approval);

    @Update("UPDATE org_approval SET deleted=1 WHERE id=#{id}")
    int deleteById(Long id);
    
    @Select("SELECT * FROM org_approval WHERE deleted=0 ORDER BY submit_time DESC")
    List<OrgApproval> findAll();
    
    @Select("SELECT * FROM org_approval WHERE id=#{id} AND deleted=0")
    OrgApproval findById(Long id);
    
    @Select("SELECT * FROM org_approval WHERE approval_no=#{approvalNo} AND deleted=0")
    OrgApproval findByApprovalNo(String approvalNo);
    
    @Select("SELECT * FROM org_approval WHERE business_id=#{businessId} AND deleted=0 ORDER BY submit_time DESC")
    List<OrgApproval> findByBusinessId(Long businessId);
    
    @Select("SELECT * FROM org_approval WHERE business_type=#{businessType} AND deleted=0 ORDER BY submit_time DESC")
    List<OrgApproval> findByBusinessType(String businessType);
    
    @Select("SELECT * FROM org_approval WHERE approval_status=#{approvalStatus} AND deleted=0 ORDER BY submit_time DESC")
    List<OrgApproval> findByApprovalStatus(String approvalStatus);
    
    @Select("SELECT * FROM org_approval WHERE current_approver=#{currentApprover} AND deleted=0 ORDER BY submit_time DESC")
    List<OrgApproval> findByCurrentApprover(String currentApprover);
    
    @Update("UPDATE org_approval SET approval_status=#{status} WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    @Update("UPDATE org_approval SET current_node=#{node}, current_approver=#{approver} WHERE id=#{id}")
    int updateCurrentNode(@Param("id") Long id, @Param("node") String node, @Param("approver") String approver);
    
    @Select("SELECT * FROM org_approval WHERE approval_status='PENDING' AND submit_time &lt; DATE_SUB(NOW(), INTERVAL #{timeoutHours} HOUR) AND deleted=0 ORDER BY submit_time DESC")
    List<OrgApproval> findTimeoutApprovals(@Param("timeoutHours") int timeoutHours);
    
    @Select("SELECT * FROM org_approval WHERE approval_status='PENDING' AND submit_time &lt; DATE_SUB(NOW(), INTERVAL #{warningHours} HOUR) AND submit_time &gt;= DATE_SUB(NOW(), INTERVAL #{timeoutHours} HOUR) AND deleted=0 ORDER BY submit_time DESC")
    List<OrgApproval> findApproachingTimeoutApprovals(@Param("timeoutHours") int timeoutHours, @Param("warningHours") int warningHours);
}
