package com.example.qualification.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.qualification.entity.QualificationStandard;
import com.example.qualification.mapper.QualificationStandardMapper;
import com.example.qualification.service.QualificationStandardService;
import org.springframework.stereotype.Service;

@Service
public class QualificationStandardServiceImpl
        extends ServiceImpl<QualificationStandardMapper, QualificationStandard>
        implements QualificationStandardService {
}
