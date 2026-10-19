package com.example.holiday.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.holiday.entity.LeaveRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 请假申请 Mapper
 */
@Mapper
public interface LeaveRequestMapper extends BaseMapper<LeaveRequest> {
}
