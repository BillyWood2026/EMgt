package com.example.iam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.iam.entity.rbac.IamRolePermission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IamRolePermissionMapper extends BaseMapper<IamRolePermission> {
}