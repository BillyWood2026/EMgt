package com.example.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.attendance.entity.FillCardRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 补卡申请 Mapper
 */
@Mapper
public interface FillCardRequestMapper extends BaseMapper<FillCardRequest> {
}
