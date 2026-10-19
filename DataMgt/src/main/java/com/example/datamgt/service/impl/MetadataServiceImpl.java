package com.example.datamgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.datamgt.entity.Metadata;
import com.example.datamgt.mapper.MetadataMapper;
import com.example.datamgt.service.MetadataService;
import org.springframework.stereotype.Service;

@Service
public class MetadataServiceImpl extends ServiceImpl<MetadataMapper, Metadata> implements MetadataService {
}
