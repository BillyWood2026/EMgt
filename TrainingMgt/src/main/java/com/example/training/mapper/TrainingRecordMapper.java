package com.example.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.training.entity.TrainingRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TrainingRecordMapper extends BaseMapper<TrainingRecord> {
}
