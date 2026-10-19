package com.example.energymgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.energymgt.entity.EnergySaving;
import com.example.energymgt.mapper.EnergySavingMapper;
import com.example.energymgt.service.EnergySavingService;
import org.springframework.stereotype.Service;

@Service
public class EnergySavingServiceImpl extends ServiceImpl<EnergySavingMapper, EnergySaving> implements EnergySavingService {
}
