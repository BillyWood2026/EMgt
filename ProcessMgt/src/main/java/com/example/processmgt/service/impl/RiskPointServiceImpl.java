package com.example.processmgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.processmgt.entity.RiskPoint;
import com.example.processmgt.mapper.RiskPointMapper;
import com.example.processmgt.service.RiskPointService;
import org.springframework.stereotype.Service;

/**
 * 流程风险点 ServiceImpl
 */
@Service
public class RiskPointServiceImpl extends ServiceImpl<RiskPointMapper, RiskPoint> implements RiskPointService {
}
