package com.example.safetymgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.safetymgt.entity.SafetyIncident;
import com.example.safetymgt.mapper.SafetyIncidentMapper;
import com.example.safetymgt.service.SafetyIncidentService;
import org.springframework.stereotype.Service;

@Service
public class SafetyIncidentServiceImpl extends ServiceImpl<SafetyIncidentMapper, SafetyIncident> implements SafetyIncidentService {
}
