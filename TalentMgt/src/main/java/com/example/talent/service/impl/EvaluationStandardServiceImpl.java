package com.example.talent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.talent.entity.EvaluationStandard;
import com.example.talent.mapper.EvaluationStandardMapper;
import com.example.talent.service.EvaluationStandardService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 胜任力标准 Service 实现
 */
@Service
public class EvaluationStandardServiceImpl
        extends ServiceImpl<EvaluationStandardMapper, EvaluationStandard>
        implements EvaluationStandardService {

    @Override
    public List<EvaluationStandard> search(String keyword, String standardName,
                                           String positionFamily, String status) {
        LambdaQueryWrapper<EvaluationStandard> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            qw.and(w -> w.like(EvaluationStandard::getStandardName, kw)
                    .or().like(EvaluationStandard::getStandardCode, kw));
        }
        if (StringUtils.hasText(standardName)) {
            qw.like(EvaluationStandard::getStandardName, standardName.trim());
        }
        if (StringUtils.hasText(positionFamily)) {
            qw.like(EvaluationStandard::getPositionFamily, positionFamily.trim());
        }
        if (StringUtils.hasText(status)) {
            qw.eq(EvaluationStandard::getStatus, status.trim());
        }
        qw.orderByDesc(EvaluationStandard::getId);
        return this.list(qw);
    }

    @Override
    public boolean removeByIdOrKey(String key) {
        if (!StringUtils.hasText(key)) {
            return false;
        }
        String k = key.trim();
        EvaluationStandard target = null;
        if (k.matches("\\d+")) {
            target = this.getById(Long.parseLong(k));
        }
        if (target == null) {
            target = this.getOne(new LambdaQueryWrapper<EvaluationStandard>()
                    .eq(EvaluationStandard::getStandardCode, k)
                    .last("LIMIT 1"));
        }
        return target != null && this.removeById(target.getId());
    }

    @Override
    public EvaluationStandard saveOrUpdateByBusinessKey(EvaluationStandard body) {
        EvaluationStandard existing = null;
        if (body.getId() != null) {
            existing = this.getById(body.getId());
        }
        if (existing == null && StringUtils.hasText(body.getStandardCode())) {
            existing = this.getOne(new LambdaQueryWrapper<EvaluationStandard>()
                    .eq(EvaluationStandard::getStandardCode, body.getStandardCode().trim())
                    .last("LIMIT 1"));
        }

        if (existing == null) {
            body.setId(null);
            body.setDeleted(0);
            body.setVersion(1);
            if (!StringUtils.hasText(body.getStatus())) {
                body.setStatus("ACTIVE");
            }
            this.save(body);
            return body;
        }

        if (StringUtils.hasText(body.getStandardCode())) {
            existing.setStandardCode(body.getStandardCode());
        }
        if (body.getStandardName() != null) {
            existing.setStandardName(body.getStandardName());
        }
        if (body.getPositionFamily() != null) {
            existing.setPositionFamily(body.getPositionFamily());
        }
        if (body.getPositionSequences() != null) {
            existing.setPositionSequences(body.getPositionSequences());
        }
        if (body.getPeriodType() != null) {
            existing.setPeriodType(body.getPeriodType());
        }
        if (body.getDescription() != null) {
            existing.setDescription(body.getDescription());
        }
        // 模型配置字段（Model.vue 编辑弹窗可改）
        if (body.getAssessType() != null) {
            existing.setAssessType(body.getAssessType());
        }
        if (body.getDuration() != null) {
            existing.setDuration(body.getDuration());
        }
        if (body.getQuestionCount() != null) {
            existing.setQuestionCount(body.getQuestionCount());
        }
        if (body.getScoringAlgo() != null) {
            existing.setScoringAlgo(body.getScoringAlgo());
        }
        if (body.getAntiCheatLevel() != null) {
            existing.setAntiCheatLevel(body.getAntiCheatLevel());
        }
        if (body.getDimWeightJson() != null) {
            existing.setDimWeightJson(body.getDimWeightJson());
        }
        if (body.getCreator() != null) {
            existing.setCreator(body.getCreator());
        }
        if (StringUtils.hasText(body.getStatus())) {
            existing.setStatus(body.getStatus());
        }
        this.updateById(existing);
        return existing;
    }
}
