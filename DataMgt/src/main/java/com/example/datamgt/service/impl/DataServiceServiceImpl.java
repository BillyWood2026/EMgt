package com.example.datamgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.datamgt.entity.DataService;
import com.example.datamgt.mapper.DataServiceMapper;
import com.example.datamgt.service.DataServiceService;
import org.springframework.stereotype.Service;

@Service
public class DataServiceServiceImpl extends ServiceImpl<DataServiceMapper, DataService> implements DataServiceService {
}
