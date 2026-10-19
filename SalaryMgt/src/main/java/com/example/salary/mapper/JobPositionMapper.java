package com.example.salary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.salary.entity.JobPosition;
import org.apache.ibatis.annotations.Mapper;

/**
 * 岗位评估 Mapper
 */
@Mapper
public interface JobPositionMapper extends BaseMapper<JobPosition> {
}
