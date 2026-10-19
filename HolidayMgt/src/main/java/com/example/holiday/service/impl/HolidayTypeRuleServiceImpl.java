package com.example.holiday.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.holiday.entity.HolidayTypeRule;
import com.example.holiday.mapper.HolidayTypeRuleMapper;
import com.example.holiday.service.HolidayTypeRuleService;
import org.springframework.stereotype.Service;

/**
 * 假期类型政策 Service 实现
 */
@Service
public class HolidayTypeRuleServiceImpl extends ServiceImpl<HolidayTypeRuleMapper, HolidayTypeRule>
        implements HolidayTypeRuleService {
}
