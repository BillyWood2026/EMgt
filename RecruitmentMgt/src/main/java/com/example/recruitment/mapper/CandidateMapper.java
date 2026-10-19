package com.example.recruitment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.recruitment.entity.Candidate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CandidateMapper extends BaseMapper<Candidate> {
}
