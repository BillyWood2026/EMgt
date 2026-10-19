package com.example.filemgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.filemgt.entity.FileRecord;
import com.example.filemgt.mapper.FileRecordMapper;
import com.example.filemgt.service.FileRecordService;
import org.springframework.stereotype.Service;

@Service
public class FileRecordServiceImpl extends ServiceImpl<FileRecordMapper, FileRecord> implements FileRecordService {
}
