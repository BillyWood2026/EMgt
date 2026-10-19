package com.example.onlinelearn.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.onlinelearn.entity.LearnTask;
import com.example.onlinelearn.mapper.LearnTaskMapper;
import com.example.onlinelearn.service.LearnTaskService;
import org.springframework.stereotype.Service;

@Service
public class LearnTaskServiceImpl extends ServiceImpl<LearnTaskMapper, LearnTask>
        implements LearnTaskService {
}
