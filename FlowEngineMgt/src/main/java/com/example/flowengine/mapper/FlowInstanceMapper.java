package com.example.flowengine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.flowengine.entity.FlowInstance;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FlowInstanceMapper extends BaseMapper<FlowInstance> {
}
