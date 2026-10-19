package com.example.datamgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.datamgt.entity.DataIntegration;
import com.example.datamgt.mapper.DataIntegrationMapper;
import com.example.datamgt.service.DataIntegrationService;
import org.springframework.stereotype.Service;

@Service
public class DataIntegrationServiceImpl extends ServiceImpl<DataIntegrationMapper, DataIntegration> implements DataIntegrationService {
}
