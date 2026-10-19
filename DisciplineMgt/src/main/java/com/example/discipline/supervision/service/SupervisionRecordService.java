package com.example.discipline.supervision.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.discipline.supervision.entity.SupervisionRecord;
import com.example.discipline.supervision.mapper.SupervisionRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SupervisionRecordService {

    @Autowired
    private SupervisionRecordMapper recordMapper;

    public Page<SupervisionRecord> page(String recordNo, String supervisionType, String riskLevel, String status,
                                        Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SupervisionRecord> qw = new LambdaQueryWrapper<>();
        if (recordNo != null && !recordNo.isEmpty()) qw.like(SupervisionRecord::getRecordNo, recordNo);
        if (supervisionType != null && !supervisionType.isEmpty()) qw.eq(SupervisionRecord::getSupervisionType, supervisionType);
        if (riskLevel != null && !riskLevel.isEmpty()) qw.eq(SupervisionRecord::getRiskLevel, riskLevel);
        if (status != null && !status.isEmpty()) qw.eq(SupervisionRecord::getStatus, status);
        qw.orderByDesc(SupervisionRecord::getId);
        return recordMapper.selectPage(new Page<>(pageNum, pageSize), qw);
    }

    public SupervisionRecord getById(Long id) {
        return recordMapper.selectById(id);
    }

    public SupervisionRecord create(SupervisionRecord r) {
        if (r.getRecordNo() == null || r.getRecordNo().isEmpty()) {
            r.setRecordNo("SR" + System.currentTimeMillis());
        }
        if (r.getStatus() == null || r.getStatus().isEmpty()) {
            r.setStatus("pending");
        }
        r.setId(null);
        r.setDeleted(null);
        r.setCreateTime(LocalDateTime.now());
        r.setUpdateTime(LocalDateTime.now());
        recordMapper.insert(r);
        return r;
    }

    public SupervisionRecord update(Long id, SupervisionRecord r) {
        r.setId(id);
        r.setUpdateTime(LocalDateTime.now());
        recordMapper.updateById(r);
        return recordMapper.selectById(id);
    }

    public boolean delete(Long id) {
        return recordMapper.deleteById(id) > 0;
    }

    /** 监督记录状态机：待处置 pending -> 处置中 rectifying -> 已完成 done。 */
    private SupervisionRecord mustGet(Long id) {
        SupervisionRecord r = recordMapper.selectById(id);
        if (r == null) {
            throw new IllegalStateException("监督记录不存在");
        }
        return r;
    }

    /** 发现问题、启动处置：待处置 -> 处置中 */
    public SupervisionRecord handle(Long id, SupervisionRecord body) {
        SupervisionRecord r = mustGet(id);
        if (!"pending".equals(r.getStatus())) {
            throw new IllegalStateException("非法状态流转：仅待处置(pending)可启动处置，当前[" + r.getStatus() + "]");
        }
        if (body != null) {
            if (body.getResponsiblePerson() != null) r.setResponsiblePerson(body.getResponsiblePerson());
            if (body.getDescription() != null) r.setDescription(body.getDescription());
        }
        r.setStatus("rectifying");
        r.setUpdateTime(LocalDateTime.now());
        recordMapper.updateById(r);
        return r;
    }

    /** 处置完成、登记处置结果：处置中 -> 已完成（须填写处置结果） */
    public SupervisionRecord complete(Long id, SupervisionRecord body) {
        SupervisionRecord r = mustGet(id);
        if (!"rectifying".equals(r.getStatus())) {
            throw new IllegalStateException("非法状态流转：仅处置中(rectifying)可办结，当前[" + r.getStatus() + "]");
        }
        String result = body == null ? null : body.getHandleResult();
        if (result == null || result.trim().isEmpty()) {
            throw new IllegalStateException("办结必须填写处置结果");
        }
        r.setHandleResult(result);
        r.setStatus("done");
        r.setUpdateTime(LocalDateTime.now());
        recordMapper.updateById(r);
        return r;
    }
}
