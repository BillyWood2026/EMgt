package com.example.talentdev.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.talentdev.entity.DevProject;
import com.example.talentdev.mapper.DevProjectMapper;
import com.example.talentdev.service.DevProjectService;
import org.springframework.stereotype.Service;

/**
 * Dev Project Service Implementation
 */
@Service
public class DevProjectServiceImpl extends ServiceImpl<DevProjectMapper, DevProject>
        implements DevProjectService {
}
