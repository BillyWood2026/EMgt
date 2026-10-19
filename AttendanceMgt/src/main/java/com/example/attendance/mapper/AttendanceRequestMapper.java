package com.example.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.attendance.entity.AttendanceRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考勤申请单（加班） Mapper
 */
@Mapper
public interface AttendanceRequestMapper extends BaseMapper<AttendanceRequest> {
}
