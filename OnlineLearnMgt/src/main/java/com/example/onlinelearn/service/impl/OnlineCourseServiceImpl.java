package com.example.onlinelearn.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.onlinelearn.entity.OnlineCourse;
import com.example.onlinelearn.mapper.OnlineCourseMapper;
import com.example.onlinelearn.service.OnlineCourseService;
import org.springframework.stereotype.Service;

@Service
public class OnlineCourseServiceImpl extends ServiceImpl<OnlineCourseMapper, OnlineCourse>
        implements OnlineCourseService {
}
