package com.example.onlineexam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.onlineexam.entity.OnlineExam;
import com.example.onlineexam.mapper.OnlineExamMapper;
import com.example.onlineexam.service.OnlineExamService;
import org.springframework.stereotype.Service;

@Service
public class OnlineExamServiceImpl extends ServiceImpl<OnlineExamMapper, OnlineExam>
        implements OnlineExamService {
}
