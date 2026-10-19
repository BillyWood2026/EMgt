package com.example.orgmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orgmgt.entity.OrgPersonalizedQuotaConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrgPersonalizedQuotaConfigMapper extends BaseMapper<OrgPersonalizedQuotaConfig> {
    
    @Select("SELECT * FROM org_personalized_quota_config WHERE org_id=#{orgId} AND deleted=0")
    OrgPersonalizedQuotaConfig selectByOrgId(@Param("orgId") Long orgId);
    
    @Select("SELECT * FROM org_personalized_quota_config WHERE org_id=#{orgId} AND position_level=#{positionLevel} AND deleted=0")
    OrgPersonalizedQuotaConfig selectByOrgAndLevel(@Param("orgId") Long orgId, @Param("positionLevel") String positionLevel);
    
    @Select("SELECT * FROM org_personalized_quota_config WHERE enabled=1 AND status='ACTIVE' AND deleted=0")
    List<OrgPersonalizedQuotaConfig> selectActiveConfigs();
    
    @Select("SELECT * FROM org_personalized_quota_config WHERE status='PENDING_APPROVAL' AND deleted=0")
    List<OrgPersonalizedQuotaConfig> selectPendingApproval();
    
    @Select("SELECT * FROM org_personalized_quota_config WHERE status='EXPIRED' AND deleted=0")
    List<OrgPersonalizedQuotaConfig> selectExpiredConfigs();
}
