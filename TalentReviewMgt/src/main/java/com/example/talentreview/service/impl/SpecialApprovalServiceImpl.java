package com.example.talentreview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.talentreview.entity.SpecialApproval;
import com.example.talentreview.mapper.SpecialApprovalMapper;
import com.example.talentreview.service.SpecialApprovalService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * 特殊审批 Service 实现
 */
@Service
public class SpecialApprovalServiceImpl
        extends ServiceImpl<SpecialApprovalMapper, SpecialApproval>
        implements SpecialApprovalService {

    /** 前端 Review.vue 遍历的状态标签 */
    private static final Set<String> KNOWN_STATUS =
            Set.of("PENDING", "IN_PROGRESS", "APPROVED", "REJECTED");

    @Override
    public List<SpecialApproval> listByStatus(String status) {
        LambdaQueryWrapper<SpecialApproval> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            String normalized = status.trim().toUpperCase();
            if (KNOWN_STATUS.contains(normalized)) {
                qw.eq(SpecialApproval::getApprovalStatus, normalized);
            }
            // 无法识别的状态值：不加过滤条件，返回全部记录
        }
        qw.orderByDesc(SpecialApproval::getId);
        return this.list(qw);
    }

    @Override
    public SpecialApproval saveOrUpdateByBusinessKey(SpecialApproval body) {
        SpecialApproval existing = null;
        if (body.getId() != null) {
            existing = this.getById(body.getId());
        }
        if (existing == null && StringUtils.hasText(body.getApprovalId())) {
            existing = this.getOne(new LambdaQueryWrapper<SpecialApproval>()
                    .eq(SpecialApproval::getApprovalId, body.getApprovalId().trim())
                    .last("LIMIT 1"));
        }

        if (existing == null) {
            body.setId(null);
            body.setDeleted(0);
            body.setVersion(1);
            if (!StringUtils.hasText(body.getApprovalId())) {
                // 审批业务编号为空时自动生成（RV开头），避免空串撞 uk_special_approval_id 唯一键
                body.setApprovalId("RV" + System.currentTimeMillis());
            }
            if (!StringUtils.hasText(body.getApprovalStatus())) {
                body.setApprovalStatus("PENDING");
            }
            this.save(body);
            return body;
        }

        if (StringUtils.hasText(body.getApprovalId())) {
            existing.setApprovalId(body.getApprovalId());
        }
        if (body.getEmployeeName() != null) {
            existing.setEmployeeName(body.getEmployeeName());
        }
        if (body.getOrgCode() != null) {
            existing.setOrgCode(body.getOrgCode());
        }
        if (body.getApprovalType() != null) {
            existing.setApprovalType(body.getApprovalType());
        }
        if (body.getReason() != null) {
            existing.setReason(body.getReason());
        }
        if (body.getHrRecommendation() != null) {
            existing.setHrRecommendation(body.getHrRecommendation());
        }
        if (body.getEffectiveDate() != null) {
            existing.setEffectiveDate(body.getEffectiveDate());
        }
        // 破格审批扩展字段（原层级/目标层级/突破项/破格标签/佐证材料）
        if (body.getOrigLevel() != null) {
            existing.setOrigLevel(body.getOrigLevel());
        }
        if (body.getTargetLevel() != null) {
            existing.setTargetLevel(body.getTargetLevel());
        }
        if (body.getBreakItems() != null) {
            existing.setBreakItems(body.getBreakItems());
        }
        if (body.getSpecialTag() != null) {
            existing.setSpecialTag(body.getSpecialTag());
        }
        if (body.getEvidence() != null) {
            existing.setEvidence(body.getEvidence());
        }
        if (StringUtils.hasText(body.getApprovalStatus())) {
            existing.setApprovalStatus(body.getApprovalStatus());
        }
        if (body.getApprover() != null) {
            existing.setApprover(body.getApprover());
        }
        if (body.getApproveTime() != null) {
            existing.setApproveTime(body.getApproveTime());
        }
        if (body.getOpinion() != null) {
            existing.setOpinion(body.getOpinion());
        }
        this.updateById(existing);
        return existing;
    }
}
