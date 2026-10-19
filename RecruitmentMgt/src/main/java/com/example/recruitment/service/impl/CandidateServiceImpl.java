package com.example.recruitment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.recruitment.entity.Candidate;
import com.example.recruitment.mapper.CandidateMapper;
import com.example.recruitment.service.CandidateService;
import org.springframework.stereotype.Service;

@Service
public class CandidateServiceImpl extends ServiceImpl<CandidateMapper, Candidate> implements CandidateService {
}
