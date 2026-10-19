package com.example.datamgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.datamgt.entity.MasterData;
import com.example.datamgt.mapper.MasterDataMapper;
import com.example.datamgt.service.MasterDataService;
import org.springframework.stereotype.Service;

@Service
public class MasterDataServiceImpl extends ServiceImpl<MasterDataMapper, MasterData> implements MasterDataService {
}
