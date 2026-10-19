package com.example.processmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.processmgt.entity.ProcessApproval;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程审批 Mapper
 */
@Mapper
public interface ProcessApprovalMapper extends BaseMapper<ProcessApproval> {
}
