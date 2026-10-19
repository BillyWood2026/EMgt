package com.example.auditmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auditmgt.entity.AuditFinding;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditFindingMapper extends BaseMapper<AuditFinding> {
}
