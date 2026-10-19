package com.example.messagemgt.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.messagemgt.message.entity.MessageRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageRecordMapper extends BaseMapper<MessageRecord> {
}
