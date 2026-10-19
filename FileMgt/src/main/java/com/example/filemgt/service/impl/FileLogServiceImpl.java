package com.example.filemgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.filemgt.entity.FileLog;
import com.example.filemgt.mapper.FileLogMapper;
import com.example.filemgt.service.FileLogService;
import org.springframework.stereotype.Service;

@Service
public class FileLogServiceImpl extends ServiceImpl<FileLogMapper, FileLog> implements FileLogService {
}
