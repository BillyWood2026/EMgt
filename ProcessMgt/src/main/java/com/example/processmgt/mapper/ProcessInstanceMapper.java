package com.example.processmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.processmgt.entity.ProcessInstance;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程实例 Mapper
 */
@Mapper
public interface ProcessInstanceMapper extends BaseMapper<ProcessInstance> {
}
