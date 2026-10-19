package com.example.onlineexam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.onlineexam.entity.ExamEvaluation;
import com.example.onlineexam.mapper.ExamEvaluationMapper;
import com.example.onlineexam.service.ExamEvaluationService;
import org.springframework.stereotype.Service;

@Service
public class ExamEvaluationServiceImpl extends ServiceImpl<ExamEvaluationMapper, ExamEvaluation>
        implements ExamEvaluationService {
}
