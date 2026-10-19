package com.example.attendance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.attendance.entity.AttendanceRequest;
import com.example.attendance.mapper.AttendanceRequestMapper;
import com.example.attendance.service.AttendanceRequestService;
import org.springframework.stereotype.Service;

/**
 * 考勤申请单（加班） Service 实现
 */
@Service
public class AttendanceRequestServiceImpl extends ServiceImpl<AttendanceRequestMapper, AttendanceRequest>
        implements AttendanceRequestService {
}
