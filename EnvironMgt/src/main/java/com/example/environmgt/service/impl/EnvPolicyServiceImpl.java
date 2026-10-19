package com.example.environmgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.environmgt.entity.EnvPolicy;
import com.example.environmgt.mapper.EnvPolicyMapper;
import com.example.environmgt.service.EnvPolicyService;
import org.springframework.stereotype.Service;

@Service
public class EnvPolicyServiceImpl extends ServiceImpl<EnvPolicyMapper, EnvPolicy> implements EnvPolicyService {
}
