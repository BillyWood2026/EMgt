package com.example.training.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.training.entity.TrainingCourse;
import com.example.training.mapper.TrainingCourseMapper;
import com.example.training.service.TrainingCourseService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class TrainingCourseServiceImpl extends ServiceImpl<TrainingCourseMapper, TrainingCourse>
        implements TrainingCourseService {

    @Override
    public List<TrainingCourse> listCourses(String courseType, String name) {
        LambdaQueryWrapper<TrainingCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(courseType), TrainingCourse::getType, courseType)
                .like(StringUtils.hasText(name), TrainingCourse::getName, name)
                .orderByDesc(TrainingCourse::getId);
        return this.list(wrapper);
    }
}
