package com.example.talent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.talent.entity.EmployeeEvaluation;
import com.example.talent.mapper.EmployeeEvaluationMapper;
import com.example.talent.service.EmployeeEvaluationService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 员工测评 Service 实现
 */
@Service
public class EmployeeEvaluationServiceImpl
        extends ServiceImpl<EmployeeEvaluationMapper, EmployeeEvaluation>
        implements EmployeeEvaluationService {

    @Override
    public List<EmployeeEvaluation> search(String keyword, String employeeName, String orgName,
                                           String batchCode, String status) {
        LambdaQueryWrapper<EmployeeEvaluation> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            qw.and(w -> w.like(EmployeeEvaluation::getEmployeeName, kw)
                    .or().like(EmployeeEvaluation::getRecordId, kw));
        }
        if (StringUtils.hasText(employeeName)) {
            qw.like(EmployeeEvaluation::getEmployeeName, employeeName.trim());
        }
        if (StringUtils.hasText(orgName)) {
            qw.like(EmployeeEvaluation::getOrgName, orgName.trim());
        }
        if (StringUtils.hasText(batchCode)) {
            qw.eq(EmployeeEvaluation::getBatchCode, batchCode.trim());
        }
        if (StringUtils.hasText(status)) {
            qw.eq(EmployeeEvaluation::getStatus, status.trim());
        }
        qw.orderByDesc(EmployeeEvaluation::getId);
        return this.list(qw);
    }

    @Override
    public boolean removeByIdOrKey(String key) {
        if (!StringUtils.hasText(key)) {
            return false;
        }
        String k = key.trim();
        EmployeeEvaluation target = null;
        if (k.matches("\\d+")) {
            target = this.getById(Long.parseLong(k));
        }
        if (target == null) {
            target = this.getOne(new LambdaQueryWrapper<EmployeeEvaluation>()
                    .eq(EmployeeEvaluation::getRecordId, k)
                    .last("LIMIT 1"));
        }
        return target != null && this.removeById(target.getId());
    }

    @Override
    public EmployeeEvaluation saveOrUpdateByBusinessKey(EmployeeEvaluation body) {
        EmployeeEvaluation existing = null;
        if (body.getId() != null) {
            existing = this.getById(body.getId());
        }
        if (existing == null && StringUtils.hasText(body.getRecordId())) {
            existing = this.getOne(new LambdaQueryWrapper<EmployeeEvaluation>()
                    .eq(EmployeeEvaluation::getRecordId, body.getRecordId().trim())
                    .last("LIMIT 1"));
        }

        if (existing == null) {
            body.setId(null);
            body.setDeleted(0);
            body.setVersion(1);
            if (!StringUtils.hasText(body.getStatus())) {
                body.setStatus("PENDING");
            }
            this.save(body);
            return body;
        }

        // 仅覆盖请求中显式提供的字段，保留未提交字段与乐观锁版本
        if (StringUtils.hasText(body.getRecordId())) {
            existing.setRecordId(body.getRecordId());
        }
        if (body.getEmployeeName() != null) {
            existing.setEmployeeName(body.getEmployeeName());
        }
        if (body.getOrgName() != null) {
            existing.setOrgName(body.getOrgName());
        }
        if (body.getPositionName() != null) {
            existing.setPositionName(body.getPositionName());
        }
        if (body.getBatchCode() != null) {
            existing.setBatchCode(body.getBatchCode());
        }
        if (body.getPerformanceScore() != null) {
            existing.setPerformanceScore(body.getPerformanceScore());
        }
        if (body.getTotalPotentialScore() != null) {
            existing.setTotalPotentialScore(body.getTotalPotentialScore());
        }
        if (body.getPerformanceTier() != null) {
            existing.setPerformanceTier(body.getPerformanceTier());
        }
        if (body.getNineBoxCategory() != null) {
            existing.setNineBoxCategory(body.getNineBoxCategory());
        }
        if (body.getManualCalibrationReason() != null) {
            existing.setManualCalibrationReason(body.getManualCalibrationReason());
        }
        // 复核/校准扩展字段（comp-assess/Evaluation.vue 复核弹窗依赖）
        if (body.getRevisedScore() != null) {
            existing.setRevisedScore(body.getRevisedScore());
        }
        if (body.getRiskLevel() != null) {
            existing.setRiskLevel(body.getRiskLevel());
        }
        if (body.getAbnormalDetail() != null) {
            existing.setAbnormalDetail(body.getAbnormalDetail());
        }
        if (body.getTalentLabel() != null) {
            existing.setTalentLabel(body.getTalentLabel());
        }
        if (body.getFitPosition() != null) {
            existing.setFitPosition(body.getFitPosition());
        }
        if (body.getReviewer() != null) {
            existing.setReviewer(body.getReviewer());
        }
        if (body.getReviewTime() != null) {
            existing.setReviewTime(body.getReviewTime());
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
