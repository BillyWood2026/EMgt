package com.example.improvemgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.improvemgt.entity.ImprovementImplementation;
import com.example.improvemgt.entity.ImprovementProposal;
import com.example.improvemgt.mapper.ImprovementImplementationMapper;
import com.example.improvemgt.mapper.ImprovementProposalMapper;
import com.example.improvemgt.service.ImprovementImplementationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ImprovementImplementationServiceImpl implements ImprovementImplementationService {

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private ImprovementImplementationMapper implementationMapper;

    @Autowired
    private ImprovementProposalMapper proposalMapper;

    @Override
    public List<ImprovementImplementation> list() {
        return implementationMapper.selectList(null);
    }

    @Override
    public IPage<ImprovementImplementation> page(Page<ImprovementImplementation> page, String status, String department) {
        LambdaQueryWrapper<ImprovementImplementation> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(ImprovementImplementation::getStatus, status);
        }
        if (department != null && !department.isEmpty()) {
            wrapper.eq(ImprovementImplementation::getDepartment, department);
        }
        wrapper.orderByDesc(ImprovementImplementation::getCreateTime);
        return implementationMapper.selectPage(page, wrapper);
    }

    @Override
    public ImprovementImplementation getById(Long id) {
        return implementationMapper.selectById(id);
    }

    @Override
    @Transactional
    public ImprovementImplementation create(ImprovementImplementation impl) {
        if (impl.getProposalId() == null) {
            throw new IllegalArgumentException("proposalId 不能为空");
        }
        ImprovementProposal proposal = proposalMapper.selectById(impl.getProposalId());
        if (proposal == null) {
            throw new IllegalArgumentException("关联提案不存在: " + impl.getProposalId());
        }
        if (!ImprovementProposal.STATUS_APPROVED.equals(proposal.getStatus())
                && !ImprovementProposal.STATUS_IMPLEMENTING.equals(proposal.getStatus())) {
            throw new IllegalStateException("仅已批准立项(approved)的提案可登记实施，提案当前状态: " + proposal.getStatus());
        }
        if (!StringUtils.hasText(impl.getImplNo())) {
            impl.setImplNo("IMP" + LocalDateTime.now().format(NO_FMT)
                    + String.format("%02d", ThreadLocalRandom.current().nextInt(100)));
        }
        if (!StringUtils.hasText(impl.getName())) {
            impl.setName(proposal.getName());
        }
        impl.setStatus(ImprovementImplementation.STATUS_PENDING);
        if (impl.getProgress() == null) {
            impl.setProgress(0);
        }
        LocalDateTime now = LocalDateTime.now();
        impl.setCreateTime(now);
        impl.setUpdateTime(now);
        impl.setDeleted(0);
        implementationMapper.insert(impl);

        // 联动提案进入实施中
        if (ImprovementProposal.STATUS_APPROVED.equals(proposal.getStatus())) {
            proposal.setStatus(ImprovementProposal.STATUS_IMPLEMENTING);
            proposal.setUpdateTime(now);
            proposalMapper.updateById(proposal);
        }
        return impl;
    }

    @Override
    @Transactional
    public ImprovementImplementation update(ImprovementImplementation impl) {
        ImprovementImplementation existing = mustGet(impl.getId());
        if (ImprovementImplementation.STATUS_COMPLETED.equals(existing.getStatus())) {
            throw new IllegalStateException("已完成的实施不能编辑");
        }
        if (impl.getProgress() != null && (impl.getProgress() < 0 || impl.getProgress() > 100)) {
            throw new IllegalArgumentException("进度必须在0~100之间: " + impl.getProgress());
        }
        impl.setUpdateTime(LocalDateTime.now());
        implementationMapper.updateById(impl);
        return implementationMapper.selectById(impl.getId());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        implementationMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        implementationMapper.deleteBatchIds(ids);
    }

    @Override
    @Transactional
    public ImprovementImplementation start(Long id) {
        return transit(id, ImprovementImplementation.STATUS_PENDING, ImprovementImplementation.STATUS_ONGOING);
    }

    @Override
    @Transactional
    public ImprovementImplementation pause(Long id) {
        return transit(id, ImprovementImplementation.STATUS_ONGOING, ImprovementImplementation.STATUS_PAUSED);
    }

    @Override
    @Transactional
    public ImprovementImplementation resume(Long id) {
        return transit(id, ImprovementImplementation.STATUS_PAUSED, ImprovementImplementation.STATUS_ONGOING);
    }

    @Override
    @Transactional
    public ImprovementImplementation complete(Long id) {
        ImprovementImplementation impl = mustGet(id);
        if (!ImprovementImplementation.STATUS_ONGOING.equals(impl.getStatus())) {
            throw new IllegalStateException("仅进行中(ongoing)的实施可完成，当前状态: " + impl.getStatus());
        }
        impl.setStatus(ImprovementImplementation.STATUS_COMPLETED);
        impl.setProgress(100);
        impl.setUpdateTime(LocalDateTime.now());
        implementationMapper.updateById(impl);

        ImprovementProposal proposal = proposalMapper.selectById(impl.getProposalId());
        if (proposal != null
                && ImprovementProposal.STATUS_IMPLEMENTING.equals(proposal.getStatus())) {
            proposal.setStatus(ImprovementProposal.STATUS_COMPLETED);
            proposal.setUpdateTime(LocalDateTime.now());
            proposalMapper.updateById(proposal);
        }
        return impl;
    }

    private ImprovementImplementation transit(Long id, String expected, String target) {
        ImprovementImplementation impl = mustGet(id);
        if (!expected.equals(impl.getStatus())) {
            throw new IllegalStateException("非法状态流转: " + impl.getStatus() + " -> " + target
                    + "，仅 " + expected + " 状态可执行该操作");
        }
        impl.setStatus(target);
        impl.setUpdateTime(LocalDateTime.now());
        implementationMapper.updateById(impl);
        return impl;
    }

    private ImprovementImplementation mustGet(Long id) {
        ImprovementImplementation impl = implementationMapper.selectById(id);
        if (impl == null) {
            throw new IllegalArgumentException("改善实施不存在: " + id);
        }
        return impl;
    }
}
