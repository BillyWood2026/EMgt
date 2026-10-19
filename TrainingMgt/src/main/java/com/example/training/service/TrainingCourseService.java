package com.example.training.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.training.entity.TrainingCourse;

import java.util.List;

public interface TrainingCourseService extends IService<TrainingCourse> {

    List<TrainingCourse> listCourses(String courseType, String name);
}
