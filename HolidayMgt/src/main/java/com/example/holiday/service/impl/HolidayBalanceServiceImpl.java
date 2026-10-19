package com.example.holiday.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.holiday.entity.HolidayBalance;
import com.example.holiday.mapper.HolidayBalanceMapper;
import com.example.holiday.service.HolidayBalanceService;
import org.springframework.stereotype.Service;

/**
 * 假期余额 Service 实现
 */
@Service
public class HolidayBalanceServiceImpl extends ServiceImpl<HolidayBalanceMapper, HolidayBalance>
        implements HolidayBalanceService {
}
