package com.example.energymgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.energymgt.entity.EnergyConsumption;
import com.example.energymgt.mapper.EnergyConsumptionMapper;
import com.example.energymgt.service.EnergyConsumptionService;
import org.springframework.stereotype.Service;

@Service
public class EnergyConsumptionServiceImpl extends ServiceImpl<EnergyConsumptionMapper, EnergyConsumption> implements EnergyConsumptionService {
}
