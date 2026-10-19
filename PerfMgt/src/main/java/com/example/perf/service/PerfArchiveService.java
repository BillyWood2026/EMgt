package com.example.perf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.perf.entity.PerfArchive;

import java.util.List;

/**
 * 绩效结果归档 Service
 */
public interface PerfArchiveService extends IService<PerfArchive> {

    /**
     * 查询全部归档结果，按排名升序
     */
    List<PerfArchive> listOrdered();

    /**
     * 详情（规避 MP selectById 生成 "rank_no AS rank" 保留字别名问题）
     */
    PerfArchive getDetail(Long id);
}
