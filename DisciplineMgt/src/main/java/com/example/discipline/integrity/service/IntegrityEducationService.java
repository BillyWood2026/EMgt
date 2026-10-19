package com.example.discipline.integrity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.discipline.integrity.entity.IntegrityEducation;
import com.example.discipline.integrity.mapper.IntegrityEducationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class IntegrityEducationService {

    @Autowired
    private IntegrityEducationMapper eduMapper;

    public Page<IntegrityEducation> page(String eduNo, String eduType, String level, String status,
                                         Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<IntegrityEducation> qw = new LambdaQueryWrapper<>();
        if (eduNo != null && !eduNo.isEmpty()) qw.like(IntegrityEducation::getEduNo, eduNo);
        if (eduType != null && !eduType.isEmpty()) qw.eq(IntegrityEducation::getEduType, eduType);
        if (level != null && !level.isEmpty()) qw.eq(IntegrityEducation::getLevel, level);
        if (status != null && !status.isEmpty()) qw.eq(IntegrityEducation::getStatus, status);
        qw.orderByDesc(IntegrityEducation::getId);
        return eduMapper.selectPage(new Page<>(pageNum, pageSize), qw);
    }

    public IntegrityEducation getById(Long id) {
        return eduMapper.selectById(id);
    }

    public IntegrityEducation create(IntegrityEducation e) {
        if (e.getEduNo() == null || e.getEduNo().isEmpty()) {
            e.setEduNo("IE" + System.currentTimeMillis());
        }
        if (e.getStatus() == null || e.getStatus().isEmpty()) {
            e.setStatus("planned");
        }
        if (e.getParticipantCount() == null) {
            e.setParticipantCount(0);
        }
        if (e.getCommitmentCount() == null) {
            e.setCommitmentCount(0);
        }
        e.setId(null);
        e.setDeleted(null);
        e.setCreateTime(LocalDateTime.now());
        e.setUpdateTime(LocalDateTime.now());
        eduMapper.insert(e);
        return e;
    }

    public IntegrityEducation update(Long id, IntegrityEducation e) {
        e.setId(id);
        e.setUpdateTime(LocalDateTime.now());
        eduMapper.updateById(e);
        return eduMapper.selectById(id);
    }

    public boolean delete(Long id) {
        return eduMapper.deleteById(id) > 0;
    }

    /** 廉政建设状态机：教育计划 planned -> 活动开展 ongoing -> 已完成 done。 */
    private IntegrityEducation mustGet(Long id) {
        IntegrityEducation e = eduMapper.selectById(id);
        if (e == null) {
            throw new IllegalStateException("廉政教育记录不存在");
        }
        return e;
    }

    /** 开展活动：计划中 -> 进行中 */
    public IntegrityEducation start(Long id, IntegrityEducation body) {
        IntegrityEducation e = mustGet(id);
        if (!"planned".equals(e.getStatus())) {
            throw new IllegalStateException("非法状态流转：仅计划中(planned)可开展活动，当前[" + e.getStatus() + "]");
        }
        if (body != null) {
            if (body.getOccurTime() != null) e.setOccurTime(body.getOccurTime());
            if (body.getResponsiblePerson() != null) e.setResponsiblePerson(body.getResponsiblePerson());
        }
        e.setStatus("ongoing");
        e.setUpdateTime(LocalDateTime.now());
        eduMapper.updateById(e);
        return e;
    }

    /** 参与/签到登记（活动进行中记录签到人数，不变更阶段） */
    public IntegrityEducation signin(Long id, IntegrityEducation body) {
        IntegrityEducation e = mustGet(id);
        if (!"ongoing".equals(e.getStatus())) {
            throw new IllegalStateException("仅进行中(ongoing)的活动可签到登记，当前[" + e.getStatus() + "]");
        }
        if (body == null || body.getParticipantCount() == null || body.getParticipantCount() < 0) {
            throw new IllegalStateException("签到必须提供非负参与人数");
        }
        e.setParticipantCount(body.getParticipantCount());
        e.setUpdateTime(LocalDateTime.now());
        eduMapper.updateById(e);
        return e;
    }

    /** 完成（宣传教育/廉政承诺）：进行中 -> 已完成，登记成效、参与与承诺人数 */
    public IntegrityEducation complete(Long id, IntegrityEducation body) {
        IntegrityEducation e = mustGet(id);
        if (!"ongoing".equals(e.getStatus())) {
            throw new IllegalStateException("非法状态流转：仅进行中(ongoing)可完成，当前[" + e.getStatus() + "]");
        }
        if (body != null) {
            if (body.getResult() != null) e.setResult(body.getResult());
            if (body.getParticipantCount() != null) e.setParticipantCount(body.getParticipantCount());
            if (body.getCommitmentCount() != null) e.setCommitmentCount(body.getCommitmentCount());
            if (body.getContent() != null) e.setContent(body.getContent());
        }
        e.setStatus("done");
        e.setUpdateTime(LocalDateTime.now());
        eduMapper.updateById(e);
        return e;
    }
}
