package com.example.talent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.talent.entity.EvaluationModel;
import com.example.talent.mapper.EvaluationModelMapper;
import com.example.talent.service.EvaluationModelService;
import org.springframework.stereotype.Service;

/**
 * Evaluation Model Service Implementation
 */
@Service
public class EvaluationModelServiceImpl extends ServiceImpl<EvaluationModelMapper, EvaluationModel>
        implements EvaluationModelService {
}
