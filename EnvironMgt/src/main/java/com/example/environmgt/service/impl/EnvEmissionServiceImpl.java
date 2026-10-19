package com.example.environmgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.environmgt.entity.EnvEmission;
import com.example.environmgt.mapper.EnvEmissionMapper;
import com.example.environmgt.service.EnvEmissionService;
import org.springframework.stereotype.Service;

@Service
public class EnvEmissionServiceImpl extends ServiceImpl<EnvEmissionMapper, EnvEmission> implements EnvEmissionService {
}
