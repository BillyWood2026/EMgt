package com.example.salary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.salary.entity.JobGrade;
import org.apache.ibatis.annotations.Mapper;

/**
 * 岗位等级 Mapper
 */
@Mapper
public interface JobGradeMapper extends BaseMapper<JobGrade> {
}
