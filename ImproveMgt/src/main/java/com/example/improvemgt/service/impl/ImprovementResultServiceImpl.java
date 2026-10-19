package com.example.improvemgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.improvemgt.entity.ImprovementImplementation;
import com.example.improvemgt.entity.ImprovementResult;
import com.example.improvemgt.mapper.ImprovementImplementationMapper;
import com.example.improvemgt.mapper.ImprovementResultMapper;
import com.example.improvemgt.service.ImprovementResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ImprovementResultServiceImpl implements ImprovementResultService {

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private ImprovementResultMapper resultMapper;

    @Autowired
    private ImprovementImplementationMapper implementationMapper;

    @Override
    public List<ImprovementResult> list() {
        return resultMapper.selectList(null);
    }

    @Override
    public IPage<ImprovementResult> page(Page<ImprovementResult> page, String status, String type) {
        LambdaQueryWrapper<ImprovementResult> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(ImprovementResult::getStatus, status);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(ImprovementResult::getType, type);
        }
        wrapper.orderByDesc(ImprovementResult::getCreateTime);
        return resultMapper.selectPage(page, wrapper);
    }

    @Override
    public ImprovementResult getById(Long id) {
        return resultMapper.selectById(id);
    }

    @Override
    @Transactional
    public ImprovementResult create(ImprovementResult result) {
        if (result.getImplId() == null) {
            throw new IllegalArgumentException("implId 不能为空");
        }
        ImprovementImplementation impl = implementationMapper.selectById(result.getImplId());
        if (impl == null) {
            throw new IllegalArgumentException("关联实施不存在: " + result.getImplId());
        }
        if (!ImprovementImplementation.STATUS_COMPLETED.equals(impl.getStatus())) {
            throw new IllegalStateException("仅已完成(completed)的实施可登记成果，实施当前状态: " + impl.getStatus());
        }
        if (!StringUtils.hasText(result.getResultNo())) {
            result.setResultNo("RES" + LocalDateTime.now().format(NO_FMT)
                    + String.format("%02d", ThreadLocalRandom.current().nextInt(100)));
        }
        if (!StringUtils.hasText(result.getName())) {
            result.setName(impl.getName());
        }
        if (result.getCompleteDate() == null) {
            result.setCompleteDate(LocalDate.now());
        }
        result.setStatus(ImprovementResult.STATUS_PENDING);
        result.setRewardStatus(ImprovementResult.REWARD_PENDING);
        applyRoi(result);
        validateScore(result.getScore());
        LocalDateTime now = LocalDateTime.now();
        result.setCreateTime(now);
        result.setUpdateTime(now);
        result.setDeleted(0);
        resultMapper.insert(result);
        return result;
    }

    @Override
    @Transactional
    public ImprovementResult update(ImprovementResult result) {
        ImprovementResult existing = mustGet(result.getId());
        if (ImprovementResult.STATUS_AWARDED.equals(existing.getStatus())
                || ImprovementResult.STATUS_ARCHIVED.equals(existing.getStatus())) {
            throw new IllegalStateException("已发放奖励/已归档的成果不能编辑，当前状态: " + existing.getStatus());
        }
        applyRoi(result);
        validateScore(result.getScore());
        result.setUpdateTime(LocalDateTime.now());
        resultMapper.updateById(result);
        return resultMapper.selectById(result.getId());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        resultMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        resultMapper.deleteBatchIds(ids);
    }

    @Override
    @Transactional
    public ImprovementResult confirm(Long id) {
        ImprovementResult result = mustGet(id);
        if (!ImprovementResult.STATUS_PENDING.equals(result.getStatus())) {
            throw new IllegalStateException("仅待确认(pending)成果可确认，当前状态: " + result.getStatus());
        }
        result.setStatus(ImprovementResult.STATUS_CONFIRMED);
        result.setUpdateTime(LocalDateTime.now());
        resultMapper.updateById(result);
        return result;
    }

    @Override
    @Transactional
    public ImprovementResult award(Long id, BigDecimal rewardAmount) {
        ImprovementResult result = mustGet(id);
        if (!ImprovementResult.STATUS_CONFIRMED.equals(result.getStatus())) {
            throw new IllegalStateException("仅已确认(confirmed)成果可发放奖励，当前状态: " + result.getStatus());
        }
        if (rewardAmount == null || rewardAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("奖励金额不能为空且不能为负");
        }
        result.setStatus(ImprovementResult.STATUS_AWARDED);
        result.setRewardStatus(ImprovementResult.REWARD_AWARDED);
        result.setRewardAmount(rewardAmount);
        LocalDateTime now = LocalDateTime.now();
        result.setRewardTime(now);
        result.setUpdateTime(now);
        resultMapper.updateById(result);
        return result;
    }

    /** ROI(%) = (实际节约 - 投入) / 投入 * 100；投入为空或0时不计算 */
    private void applyRoi(ImprovementResult result) {
        BigDecimal investment = result.getInvestmentAmount();
        BigDecimal savings = result.getActualSavings();
        if (investment != null && investment.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal base = savings == null ? BigDecimal.ZERO : savings;
            result.setRoi(base.subtract(investment).multiply(new BigDecimal("100"))
                    .divide(investment, 2, RoundingMode.HALF_UP));
        }
    }

    private void validateScore(Integer score) {
        if (score != null && (score < 1 || score > 5)) {
            throw new IllegalArgumentException("评分必须在1~5之间: " + score);
        }
    }

    private ImprovementResult mustGet(Long id) {
        ImprovementResult result = resultMapper.selectById(id);
        if (result == null) {
            throw new IllegalArgumentException("改善成果不存在: " + id);
        }
        return result;
    }
}
