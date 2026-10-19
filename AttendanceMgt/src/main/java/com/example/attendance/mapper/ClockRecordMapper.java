package com.example.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.attendance.entity.ClockRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 日常打卡 Mapper
 */
@Mapper
public interface ClockRecordMapper extends BaseMapper<ClockRecord> {
}
