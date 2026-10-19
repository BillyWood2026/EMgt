package com.example.improvemgt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.improvemgt.entity.ImprovementResult;

import java.math.BigDecimal;
import java.util.List;

public interface ImprovementResultService {

    List<ImprovementResult> list();

    IPage<ImprovementResult> page(Page<ImprovementResult> page, String status, String type);

    ImprovementResult getById(Long id);

    ImprovementResult create(ImprovementResult result);

    ImprovementResult update(ImprovementResult result);

    void deleteById(Long id);

    void batchDelete(List<Long> ids);

    /** pending -> confirmed */
    ImprovementResult confirm(Long id);

    /** confirmed -> awarded，发放奖励并记录金额/时间 */
    ImprovementResult award(Long id, BigDecimal rewardAmount);
}
