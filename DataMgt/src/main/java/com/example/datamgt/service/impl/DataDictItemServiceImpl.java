package com.example.datamgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.datamgt.entity.DataDictItem;
import com.example.datamgt.mapper.DataDictItemMapper;
import com.example.datamgt.service.DataDictItemService;
import org.springframework.stereotype.Service;

@Service
public class DataDictItemServiceImpl extends ServiceImpl<DataDictItemMapper, DataDictItem> implements DataDictItemService {
}
