package com.example.energymgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.energymgt.entity.EnergyAnalysis;
import com.example.energymgt.mapper.EnergyAnalysisMapper;
import com.example.energymgt.service.EnergyAnalysisService;
import org.springframework.stereotype.Service;

@Service
public class EnergyAnalysisServiceImpl extends ServiceImpl<EnergyAnalysisMapper, EnergyAnalysis> implements EnergyAnalysisService {
}
