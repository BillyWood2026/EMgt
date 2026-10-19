package com.example.improvemgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.improvemgt.entity.ImprovementProposal;
import com.example.improvemgt.mapper.ImprovementProposalMapper;
import com.example.improvemgt.service.ImprovementProposalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ImprovementProposalServiceImpl implements ImprovementProposalService {

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private ImprovementProposalMapper proposalMapper;

    @Override
    public List<ImprovementProposal> list() {
        return proposalMapper.selectList(null);
    }

    @Override
    public IPage<ImprovementProposal> page(Page<ImprovementProposal> page, String status, String type, String department) {
        LambdaQueryWrapper<ImprovementProposal> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(ImprovementProposal::getStatus, status);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(ImprovementProposal::getType, type);
        }
        if (department != null && !department.isEmpty()) {
            wrapper.eq(ImprovementProposal::getDepartment, department);
        }
        wrapper.orderByDesc(ImprovementProposal::getCreateTime);
        return proposalMapper.selectPage(page, wrapper);
    }

    @Override
    public ImprovementProposal getById(Long id) {
        return proposalMapper.selectById(id);
    }

    @Override
    @Transactional
    public ImprovementProposal create(ImprovementProposal proposal) {
        if (!StringUtils.hasText(proposal.getProposalNo())) {
            proposal.setProposalNo("IP" + LocalDateTime.now().format(NO_FMT)
                    + String.format("%02d", ThreadLocalRandom.current().nextInt(100)));
        }
        proposal.setStatus(ImprovementProposal.STATUS_PENDING);
        LocalDateTime now = LocalDateTime.now();
        proposal.setSubmitTime(now);
        proposal.setCreateTime(now);
        proposal.setUpdateTime(now);
        proposal.setDeleted(0);
        proposalMapper.insert(proposal);
        return proposal;
    }

    @Override
    @Transactional
    public ImprovementProposal update(ImprovementProposal proposal) {
        ImprovementProposal existing = mustGet(proposal.getId());
        if (ImprovementProposal.STATUS_APPROVED.equals(existing.getStatus())
                || ImprovementProposal.STATUS_IMPLEMENTING.equals(existing.getStatus())
                || ImprovementProposal.STATUS_COMPLETED.equals(existing.getStatus())) {
            throw new IllegalStateException("已立项/实施中的提案不能直接编辑，当前状态: " + existing.getStatus());
        }
        proposal.setUpdateTime(LocalDateTime.now());
        proposalMapper.updateById(proposal);
        return proposalMapper.selectById(proposal.getId());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        proposalMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        proposalMapper.deleteBatchIds(ids);
    }

    @Override
    @Transactional
    public ImprovementProposal review(Long id, boolean approved, String comment) {
        ImprovementProposal proposal = mustGet(id);
        if (!ImprovementProposal.STATUS_PENDING.equals(proposal.getStatus())) {
            throw new IllegalStateException("仅待评审(pending)提案可评审，当前状态: " + proposal.getStatus());
        }
        proposal.setStatus(approved
                ? ImprovementProposal.STATUS_APPROVED
                : ImprovementProposal.STATUS_REJECTED);
        proposal.setApprovalComment(comment);
        proposal.setUpdateTime(LocalDateTime.now());
        proposalMapper.updateById(proposal);
        return proposal;
    }

    @Override
    @Transactional
    public ImprovementProposal markImplementing(Long id) {
        ImprovementProposal proposal = mustGet(id);
        if (!ImprovementProposal.STATUS_APPROVED.equals(proposal.getStatus())) {
            throw new IllegalStateException("仅已批准立项(approved)提案可进入实施，当前状态: " + proposal.getStatus());
        }
        proposal.setStatus(ImprovementProposal.STATUS_IMPLEMENTING);
        proposal.setUpdateTime(LocalDateTime.now());
        proposalMapper.updateById(proposal);
        return proposal;
    }

    @Override
    @Transactional
    public ImprovementProposal markCompleted(Long id) {
        ImprovementProposal proposal = mustGet(id);
        if (!ImprovementProposal.STATUS_IMPLEMENTING.equals(proposal.getStatus())) {
            throw new IllegalStateException("仅实施中(implementing)提案可完成，当前状态: " + proposal.getStatus());
        }
        proposal.setStatus(ImprovementProposal.STATUS_COMPLETED);
        proposal.setUpdateTime(LocalDateTime.now());
        proposalMapper.updateById(proposal);
        return proposal;
    }

    private ImprovementProposal mustGet(Long id) {
        ImprovementProposal proposal = proposalMapper.selectById(id);
        if (proposal == null) {
            throw new IllegalArgumentException("改善提案不存在: " + id);
        }
        return proposal;
    }
}
