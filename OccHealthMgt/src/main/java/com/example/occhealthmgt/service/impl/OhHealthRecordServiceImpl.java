package com.example.occhealthmgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.occhealthmgt.entity.OhHealthRecord;
import com.example.occhealthmgt.mapper.OhHealthRecordMapper;
import com.example.occhealthmgt.service.OhHealthRecordService;
import org.springframework.stereotype.Service;

@Service
public class OhHealthRecordServiceImpl extends ServiceImpl<OhHealthRecordMapper, OhHealthRecord> implements OhHealthRecordService {
}
