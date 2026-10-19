package com.example.flowengine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.flowengine.entity.FlowNode;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FlowNodeMapper extends BaseMapper<FlowNode> {
}
