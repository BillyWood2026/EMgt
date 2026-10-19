package com.example.auditmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auditmgt.entity.AuditReport;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditReportMapper extends BaseMapper<AuditReport> {
}
