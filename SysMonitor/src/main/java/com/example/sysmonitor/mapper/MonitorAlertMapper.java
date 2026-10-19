package com.example.sysmonitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sysmonitor.entity.MonitorAlert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MonitorAlertMapper extends BaseMapper<MonitorAlert> {
}
