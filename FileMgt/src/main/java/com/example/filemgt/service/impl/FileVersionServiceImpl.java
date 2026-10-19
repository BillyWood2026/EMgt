package com.example.filemgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.filemgt.entity.FileVersion;
import com.example.filemgt.mapper.FileVersionMapper;
import com.example.filemgt.service.FileVersionService;
import org.springframework.stereotype.Service;

@Service
public class FileVersionServiceImpl extends ServiceImpl<FileVersionMapper, FileVersion> implements FileVersionService {
}
