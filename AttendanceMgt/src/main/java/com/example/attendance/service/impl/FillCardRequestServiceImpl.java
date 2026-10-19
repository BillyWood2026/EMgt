package com.example.attendance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.attendance.entity.FillCardRequest;
import com.example.attendance.mapper.FillCardRequestMapper;
import com.example.attendance.service.FillCardRequestService;
import org.springframework.stereotype.Service;

/**
 * 补卡申请 Service 实现
 */
@Service
public class FillCardRequestServiceImpl extends ServiceImpl<FillCardRequestMapper, FillCardRequest>
        implements FillCardRequestService {
}
