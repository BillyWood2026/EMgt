package com.example.talentreview.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.talentreview.entity.SpecialApproval;

import java.util.List;

/**
 * 特殊审批 Service
 */
public interface SpecialApprovalService extends IService<SpecialApproval> {

    /**
     * 按审批状态查询；无法识别的状态值返回全部记录（容忍任意 {status} 路径变量）
     */
    List<SpecialApproval> listByStatus(String status);

    /**
     * 创建或更新：优先按 body.id，其次按 approvalId 定位已有记录
     */
    SpecialApproval saveOrUpdateByBusinessKey(SpecialApproval body);
}
