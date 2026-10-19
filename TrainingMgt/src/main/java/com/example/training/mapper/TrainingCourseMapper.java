package com.example.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.training.entity.TrainingCourse;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TrainingCourseMapper extends BaseMapper<TrainingCourse> {
}
