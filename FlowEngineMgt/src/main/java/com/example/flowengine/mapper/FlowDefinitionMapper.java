package com.example.flowengine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.flowengine.entity.FlowDefinition;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FlowDefinitionMapper extends BaseMapper<FlowDefinition> {
}
