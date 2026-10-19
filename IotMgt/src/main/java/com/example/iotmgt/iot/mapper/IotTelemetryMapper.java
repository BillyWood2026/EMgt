package com.example.iotmgt.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.iotmgt.iot.entity.IotTelemetry;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IotTelemetryMapper extends BaseMapper<IotTelemetry> {
}
