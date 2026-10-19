package com.example.datamgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.datamgt.entity.DataQuality;
import com.example.datamgt.mapper.DataQualityMapper;
import com.example.datamgt.service.DataQualityService;
import org.springframework.stereotype.Service;

@Service
public class DataQualityServiceImpl extends ServiceImpl<DataQualityMapper, DataQuality> implements DataQualityService {
}
