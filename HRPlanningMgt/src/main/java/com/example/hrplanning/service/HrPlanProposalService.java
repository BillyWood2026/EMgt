package com.example.hrplanning.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.hrplanning.entity.HrPlanProposal;

public interface HrPlanProposalService extends IService<HrPlanProposal> {

    IPage<HrPlanProposal> page(Integer pageNum, Integer pageSize, String orgCode, String status);
}
