package com.example.datamgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.datamgt.entity.DataDictType;
import com.example.datamgt.mapper.DataDictTypeMapper;
import com.example.datamgt.service.DataDictTypeService;
import org.springframework.stereotype.Service;

@Service
public class DataDictTypeServiceImpl extends ServiceImpl<DataDictTypeMapper, DataDictType> implements DataDictTypeService {
}
