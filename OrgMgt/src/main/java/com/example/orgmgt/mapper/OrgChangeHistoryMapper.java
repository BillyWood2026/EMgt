package com.example.orgmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orgmgt.entity.OrgChangeHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OrgChangeHistoryMapper extends BaseMapper<OrgChangeHistory> {
    
    @Select("SELECT * FROM org_change_history WHERE deleted=0 ORDER BY created_time DESC")
    List<OrgChangeHistory> findAll();
    
    @Select("SELECT * FROM org_change_history WHERE id=#{id} AND deleted=0")
    OrgChangeHistory findById(Long id);
    
    @Select("SELECT * FROM org_change_history WHERE change_no=#{changeNo} AND deleted=0")
    OrgChangeHistory findByChangeNo(String changeNo);
    
    @Select("SELECT * FROM org_change_history WHERE org_id=#{orgId} AND deleted=0 ORDER BY created_time DESC")
    List<OrgChangeHistory> findByOrgId(Long orgId);
    
    @Select("SELECT * FROM org_change_history WHERE org_code=#{orgCode} AND deleted=0 ORDER BY created_time DESC")
    List<OrgChangeHistory> findByOrgCode(String orgCode);
    
    @Select("SELECT * FROM org_change_history WHERE change_type=#{changeType} AND deleted=0 ORDER BY created_time DESC")
    List<OrgChangeHistory> findByChangeType(String changeType);
    
    @Select("SELECT * FROM org_change_history WHERE approval_status=#{approvalStatus} AND deleted=0 ORDER BY created_time DESC")
    List<OrgChangeHistory> findByApprovalStatus(String approvalStatus);
    
    @Select("SELECT * FROM org_change_history WHERE created_time BETWEEN #{startDate} AND #{endDate} AND deleted=0 ORDER BY created_time DESC")
    List<OrgChangeHistory> findByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);
    
    @Update("UPDATE org_change_history SET approval_status=#{status} WHERE id=#{id}")
    int updateApprovalStatus(@Param("id") Long id, @Param("status") String status);
}
