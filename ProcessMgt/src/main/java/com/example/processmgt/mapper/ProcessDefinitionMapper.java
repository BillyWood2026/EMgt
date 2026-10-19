package com.example.processmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.processmgt.entity.ProcessDefinition;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程定义 Mapper
 */
@Mapper
public interface ProcessDefinitionMapper extends BaseMapper<ProcessDefinition> {
}
