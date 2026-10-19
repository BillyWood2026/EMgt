package com.example.safetymgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.safetymgt.entity.SafetyInspection;
import com.example.safetymgt.mapper.SafetyInspectionMapper;
import com.example.safetymgt.service.SafetyInspectionService;
import org.springframework.stereotype.Service;

@Service
public class SafetyInspectionServiceImpl extends ServiceImpl<SafetyInspectionMapper, SafetyInspection> implements SafetyInspectionService {
}
