package com.example.safetymgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.safetymgt.entity.SafetyPolicy;
import com.example.safetymgt.mapper.SafetyPolicyMapper;
import com.example.safetymgt.service.SafetyPolicyService;
import org.springframework.stereotype.Service;

@Service
public class SafetyPolicyServiceImpl extends ServiceImpl<SafetyPolicyMapper, SafetyPolicy> implements SafetyPolicyService {
}
