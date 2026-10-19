package com.example.talentdev.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.talentdev.entity.DevTracking;
import com.example.talentdev.mapper.DevTrackingMapper;
import com.example.talentdev.service.DevTrackingService;
import org.springframework.stereotype.Service;

/**
 * Dev Tracking Service Implementation
 */
@Service
public class DevTrackingServiceImpl extends ServiceImpl<DevTrackingMapper, DevTracking>
        implements DevTrackingService {
}
