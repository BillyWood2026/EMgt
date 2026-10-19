package com.example.datamgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.datamgt.entity.DataStandard;
import com.example.datamgt.mapper.DataStandardMapper;
import com.example.datamgt.service.DataStandardService;
import org.springframework.stereotype.Service;

@Service
public class DataStandardServiceImpl extends ServiceImpl<DataStandardMapper, DataStandard> implements DataStandardService {
}
