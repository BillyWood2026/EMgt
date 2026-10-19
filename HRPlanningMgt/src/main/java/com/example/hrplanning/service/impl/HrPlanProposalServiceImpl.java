package com.example.hrplanning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.hrplanning.entity.HrPlanProposal;
import com.example.hrplanning.mapper.HrPlanProposalMapper;
import com.example.hrplanning.service.HrPlanProposalService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class HrPlanProposalServiceImpl
        extends ServiceImpl<HrPlanProposalMapper, HrPlanProposal>
        implements HrPlanProposalService {

    @Override
    public IPage<HrPlanProposal> page(Integer pageNum, Integer pageSize, String orgCode, String status) {
        LambdaQueryWrapper<HrPlanProposal> wrapper = new LambdaQueryWrapper<HrPlanProposal>()
                .eq(StringUtils.hasText(orgCode), HrPlanProposal::getOrgCode, orgCode)
                .eq(StringUtils.hasText(status), HrPlanProposal::getStatus, status)
                .orderByDesc(HrPlanProposal::getId);
        return baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }
}
