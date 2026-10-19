package com.example.environmgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.environmgt.entity.EnvTarget;
import com.example.environmgt.mapper.EnvTargetMapper;
import com.example.environmgt.service.EnvTargetService;
import org.springframework.stereotype.Service;

@Service
public class EnvTargetServiceImpl extends ServiceImpl<EnvTargetMapper, EnvTarget> implements EnvTargetService {
}
