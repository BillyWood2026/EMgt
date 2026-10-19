package com.example.talent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.talent.entity.EmployeeEvaluation;

import java.util.List;

/**
 * 员工测评 Service
 */
public interface EmployeeEvaluationService extends IService<EmployeeEvaluation> {

    /**
     * 列表搜索（全部条件可选；keyword 同时匹配员工姓名/业务编号）
     */
    List<EmployeeEvaluation> search(String keyword, String employeeName, String orgName,
                                    String batchCode, String status);

    /**
     * 按主键 id 或业务编号 recordId 删除（前端行 id 即 recordId）
     */
    boolean removeByIdOrKey(String key);

    /**
     * 创建或更新：优先按 body.id，其次按 recordId 定位已有记录
     */
    EmployeeEvaluation saveOrUpdateByBusinessKey(EmployeeEvaluation body);
}
