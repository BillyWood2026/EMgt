package com.example.talent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.talent.entity.EvaluationStandard;

import java.util.List;

/**
 * 胜任力标准 Service
 */
public interface EvaluationStandardService extends IService<EvaluationStandard> {

    /**
     * 列表搜索（全部条件可选；keyword 同时匹配模型名称/业务编号）
     */
    List<EvaluationStandard> search(String keyword, String standardName,
                                    String positionFamily, String status);

    /**
     * 按主键 id 或模型编号 standardCode 删除
     */
    boolean removeByIdOrKey(String key);

    /**
     * 创建或更新：优先按 body.id，其次按 standardCode 定位已有记录
     */
    EvaluationStandard saveOrUpdateByBusinessKey(EvaluationStandard body);
}
