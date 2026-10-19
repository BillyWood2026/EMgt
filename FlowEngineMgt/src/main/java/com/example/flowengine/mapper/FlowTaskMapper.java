package com.example.flowengine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.flowengine.entity.FlowTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FlowTaskMapper extends BaseMapper<FlowTask> {
}
