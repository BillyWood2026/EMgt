package com.example.holiday.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.holiday.entity.LeaveRequest;
import com.example.holiday.mapper.LeaveRequestMapper;
import com.example.holiday.service.LeaveRequestService;
import org.springframework.stereotype.Service;

/**
 * 请假申请 Service 实现
 */
@Service
public class LeaveRequestServiceImpl extends ServiceImpl<LeaveRequestMapper, LeaveRequest>
        implements LeaveRequestService {
}
