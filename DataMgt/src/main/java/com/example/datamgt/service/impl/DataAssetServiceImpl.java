package com.example.datamgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.datamgt.entity.DataAsset;
import com.example.datamgt.mapper.DataAssetMapper;
import com.example.datamgt.service.DataAssetService;
import org.springframework.stereotype.Service;

@Service
public class DataAssetServiceImpl extends ServiceImpl<DataAssetMapper, DataAsset> implements DataAssetService {
}
