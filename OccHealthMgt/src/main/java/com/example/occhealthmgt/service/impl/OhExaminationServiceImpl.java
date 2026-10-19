package com.example.occhealthmgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.occhealthmgt.entity.OhExamination;
import com.example.occhealthmgt.mapper.OhExaminationMapper;
import com.example.occhealthmgt.service.OhExaminationService;
import org.springframework.stereotype.Service;

@Service
public class OhExaminationServiceImpl extends ServiceImpl<OhExaminationMapper, OhExamination> implements OhExaminationService {
}
