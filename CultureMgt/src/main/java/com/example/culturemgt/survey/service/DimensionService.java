package com.example.culturemgt.survey.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.common.cul.CurrentHeader;
import com.example.culturemgt.survey.entity.SurveyDimension;
import com.example.culturemgt.survey.mapper.SurveyDimensionMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 氛围维度与权重配置（算法61 输入；启用维度权重合计必须=100）。 */
@Service
public class DimensionService {

    @Autowired private SurveyDimensionMapper mapper;
    @Autowired private AuditService audit;

    public List<SurveyDimension> list(String status) {
        LambdaQueryWrapper<SurveyDimension> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            w.eq(SurveyDimension::getStatus, status);
        }
        w.orderByAsc(SurveyDimension::getId);
        return mapper.selectList(w);
    }

    @Transactional
    public SurveyDimension save(SurveyDimension d) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        SurveyFlow.require(SurveyFlow.len(d.getDimensionName()) >= 2 && SurveyFlow.len(d.getDimensionName()) <= 50,
                "01", "维度名称需2-50字");
        SurveyFlow.require(d.getWeight() != null && d.getWeight().doubleValue() >= 0 && d.getWeight().doubleValue() <= 100,
                "17", "维度权重须在0-100之间");
        String status = d.getStatus() == null || d.getStatus().isBlank() ? "启用" : d.getStatus();
        SurveyFlow.require("启用".equals(status) || "停用".equals(status), "01", "维度状态须为 启用/停用");
        // 名称查重
        SurveyDimension dup = mapper.selectOne(new LambdaQueryWrapper<SurveyDimension>()
                .eq(SurveyDimension::getDimensionName, d.getDimensionName().trim())
                .eq(SurveyDimension::getDeleted, 0).last("limit 1"));
        SurveyFlow.require(dup == null || dup.getId().equals(d.getId()), "18", "维度名称已存在");

        LocalDateTime now = LocalDateTime.now();
        if (d.getId() == null) {
            d.setId(null);
            d.setStatus(status);
            d.setDimensionName(d.getDimensionName().trim());
            d.setOperator(CurrentHeader.operator());
            mapper.insert(d);
            audit.log("NEW", "cul_survey_dimension", d.getId(), "新增氛围维度：" + d.getDimensionName());
        } else {
            SurveyDimension old = mapper.selectById(d.getId());
            SurveyFlow.require(old != null, "01", "维度不存在");
            old.setDimensionName(d.getDimensionName().trim());
            old.setWeight(d.getWeight());
            old.setStatus(status);
            old.setRemark(d.getRemark());
            old.setOperator(CurrentHeader.operator());
            old.setUpdateTime(now);
            mapper.updateById(old);
            audit.log("EDIT", "cul_survey_dimension", d.getId(), "编辑氛围维度：" + old.getDimensionName());
        }
        // 保存后校验启用项权重合计=100
        BigDecimal sum = mapper.selectList(new LambdaQueryWrapper<SurveyDimension>()
                        .eq(SurveyDimension::getStatus, "启用").eq(SurveyDimension::getDeleted, 0))
                .stream().map(SurveyDimension::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        SurveyFlow.require(sum.compareTo(new BigDecimal("100")) == 0,
                "17", "启用维度权重合计须=100，当前=" + sum.stripTrailingZeros().toPlainString());
        return d;
    }

    /** 停用/启用切换（切换后仍须满足合计=100）。 */
    @Transactional
    public SurveyDimension toggle(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        SurveyDimension d = mapper.selectById(id);
        SurveyFlow.require(d != null, "01", "维度不存在");
        d.setStatus("启用".equals(d.getStatus()) ? "停用" : "启用");
        d.setOperator(CurrentHeader.operator());
        mapper.updateById(d);
        // 无论启用/停用，切换后启用维度合计必须=100（停用前请先把该维度权重调整到其余维度）
        BigDecimal sum = mapper.selectList(new LambdaQueryWrapper<SurveyDimension>()
                        .eq(SurveyDimension::getStatus, "启用").eq(SurveyDimension::getDeleted, 0))
                .stream().map(SurveyDimension::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        SurveyFlow.require(sum.compareTo(new BigDecimal("100")) == 0,
                "17", "切换后启用维度权重合计须=100，当前=" + sum.stripTrailingZeros().toPlainString()
                        + "，请先调整各维度权重");
        audit.log("TOGGLE", "cul_survey_dimension", id, "维度切换为" + d.getStatus() + "：" + d.getDimensionName());
        return d;
    }
}
