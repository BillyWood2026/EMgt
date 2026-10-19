package com.example.environmgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.environmgt.entity.EnvWaste;
import com.example.environmgt.mapper.EnvWasteMapper;
import com.example.environmgt.service.EnvWasteService;
import org.springframework.stereotype.Service;

@Service
public class EnvWasteServiceImpl extends ServiceImpl<EnvWasteMapper, EnvWaste> implements EnvWasteService {
}
