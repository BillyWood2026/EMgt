package com.example.perf.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.perf.entity.PerfArchive;
import com.example.perf.mapper.PerfArchiveMapper;
import com.example.perf.service.PerfArchiveService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 绩效结果归档 Service 实现
 */
@Service
public class PerfArchiveServiceImpl extends ServiceImpl<PerfArchiveMapper, PerfArchive> implements PerfArchiveService {

    @Override
    public List<PerfArchive> listOrdered() {
        return baseMapper.listOrdered();
    }

    @Override
    public PerfArchive getDetail(Long id) {
        return baseMapper.selectDetailById(id);
    }
}
