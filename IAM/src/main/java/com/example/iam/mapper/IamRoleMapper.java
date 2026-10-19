package com.example.iam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.iam.entity.rbac.IamRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IamRoleMapper extends BaseMapper<IamRole> {
}