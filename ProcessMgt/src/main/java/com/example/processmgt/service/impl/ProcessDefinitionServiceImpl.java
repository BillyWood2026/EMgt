package com.example.processmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.processmgt.entity.ProcessApproval;
import com.example.processmgt.entity.ProcessDefinition;
import com.example.processmgt.mapper.ProcessApprovalMapper;
import com.example.processmgt.mapper.ProcessDefinitionMapper;
import com.example.processmgt.service.ProcessDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 流程定义 Service 实现
 */
@Service
public class ProcessDefinitionServiceImpl extends ServiceImpl<ProcessDefinitionMapper, ProcessDefinition>
        implements ProcessDefinitionService {

    public static final String STATUS_REJECTED = "rejected";

    @Autowired
    private ProcessApprovalMapper processApprovalMapper;

    @Override
    public IPage<ProcessDefinition> pageList(Integer pageNum, Integer pageSize, String keyword,
                                              String level, String status, String category) {
        Page<ProcessDefinition> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProcessDefinition> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ProcessDefinition::getName, keyword)
                    .or().like(ProcessDefinition::getCode, keyword)
                    .or().like(ProcessDefinition::getDescription, keyword));
        }
        if (StringUtils.hasText(level)) {
            wrapper.eq(ProcessDefinition::getLevel, level);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(ProcessDefinition::getStatus, status);
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(ProcessDefinition::getCategory, category);
        }
        wrapper.orderByDesc(ProcessDefinition::getUpdateTime);
        return this.page(page, wrapper);
    }

    @Override
    public void deprecate(Long id) {
        ProcessDefinition def = mustGet(id);
        def.setStatus(ProcessDefinition.STATUS_DEPRECATED);
        def.setUpdateTime(LocalDateTime.now());
        this.updateById(def);
    }

    @Override
    public void archive(Long id) {
        ProcessDefinition def = mustGet(id);
        def.setStatus(ProcessDefinition.STATUS_ARCHIVED);
        def.setUpdateTime(LocalDateTime.now());
        this.updateById(def);
    }

    @Override
    @Transactional
    public ProcessDefinition submitReview(Long id) {
        ProcessDefinition def = mustGet(id);
        String status = def.getStatus();
        if (!ProcessDefinition.STATUS_DRAFT.equals(status) && !STATUS_REJECTED.equals(status)) {
            throw new IllegalStateException("仅草稿或被驳回的流程可提交评审，当前状态: " + status);
        }
        def.setStatus(ProcessDefinition.STATUS_PENDING);
        def.setUpdateTime(LocalDateTime.now());
        this.updateById(def);

        // 生成评审审批记录(同一流程定义若存在未处理记录则不再重复生成)
        Long existCount = processApprovalMapper.selectCount(
                new LambdaQueryWrapper<ProcessApproval>()
                        .eq(ProcessApproval::getProcessDefId, id)
                        .eq(ProcessApproval::getBizType, "DEF")
                        .eq(ProcessApproval::getStatus, ProcessApproval.STATUS_PENDING));
        if (existCount == null || existCount == 0L) {
            ProcessApproval approval = new ProcessApproval();
            approval.setProcessDefId(id);
            approval.setBizType("DEF");
            approval.setProcessName(def.getName());
            approval.setApplicant(StringUtils.hasText(def.getApprover()) ? def.getApprover() : "设计人");
            approval.setApprover(StringUtils.hasText(def.getApprover()) ? def.getApprover() : "评审人");
            approval.setNodeName("流程评审");
            approval.setStatus(ProcessApproval.STATUS_PENDING);
            approval.setCreateTime(LocalDateTime.now());
            approval.setUpdateTime(LocalDateTime.now());
            processApprovalMapper.insert(approval);
        }
        return def;
    }

    @Override
    @Transactional
    public ProcessDefinition approve(Long id, String comment) {
        ProcessDefinition def = mustGet(id);
        if (!ProcessDefinition.STATUS_PENDING.equals(def.getStatus())) {
            throw new IllegalStateException("仅待评审(pending)流程可审批通过，当前状态: " + def.getStatus());
        }
        def.setStatus(ProcessDefinition.STATUS_APPROVED);
        def.setUpdateTime(LocalDateTime.now());
        this.updateById(def);
        return def;
    }

    @Override
    @Transactional
    public ProcessDefinition reject(Long id, String comment) {
        ProcessDefinition def = mustGet(id);
        if (!ProcessDefinition.STATUS_PENDING.equals(def.getStatus())) {
            throw new IllegalStateException("仅待评审(pending)流程可驳回，当前状态: " + def.getStatus());
        }
        def.setStatus(STATUS_REJECTED);
        def.setUpdateTime(LocalDateTime.now());
        this.updateById(def);
        return def;
    }

    @Override
    @Transactional
    public ProcessDefinition publish(Long id) {
        ProcessDefinition def = mustGet(id);
        if (!ProcessDefinition.STATUS_APPROVED.equals(def.getStatus())) {
            throw new IllegalStateException("仅评审通过(approved)的流程可发布，当前状态: " + def.getStatus());
        }
        def.setStatus(ProcessDefinition.STATUS_ACTIVE);
        if (!StringUtils.hasText(def.getVersion())) {
            def.setVersion("V1.0");
        }
        def.setUpdateTime(LocalDateTime.now());
        this.updateById(def);
        return def;
    }

    @Override
    @Transactional
    public ProcessDefinition newVersion(Long id) {
        ProcessDefinition source = mustGet(id);
        if (!ProcessDefinition.STATUS_ACTIVE.equals(source.getStatus())
                && !ProcessDefinition.STATUS_DEPRECATED.equals(source.getStatus())) {
            throw new IllegalStateException("仅已发布(active)流程可发起变更产生新版本，当前状态: " + source.getStatus());
        }
        ProcessDefinition copy = new ProcessDefinition();
        copy.setCode(source.getCode());
        copy.setName(source.getName());
        copy.setLevel(source.getLevel());
        copy.setCategory(source.getCategory());
        copy.setOwnerDept(source.getOwnerDept());
        copy.setApprover(source.getApprover());
        copy.setDescription(source.getDescription());
        copy.setContent(source.getContent());
        copy.setStatus(ProcessDefinition.STATUS_DRAFT);
        copy.setVersion(bumpVersion(source.getVersion()));
        copy.setCreateTime(LocalDateTime.now());
        copy.setUpdateTime(LocalDateTime.now());
        this.save(copy);
        return copy;
    }

    /**
     * V3.0 -> V3.1；无法解析时追加 .1
     */
    private String bumpVersion(String version) {
        if (!StringUtils.hasText(version)) {
            return "V1.0";
        }
        String v = version.trim();
        boolean leadingV = v.toLowerCase().startsWith("v");
        String num = leadingV ? v.substring(1) : v;
        String[] parts = num.split("\\.");
        try {
            int major = Integer.parseInt(parts[0]);
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            return (leadingV ? "V" : "") + major + "." + (minor + 1);
        } catch (NumberFormatException e) {
            return version + ".1";
        }
    }

    private ProcessDefinition mustGet(Long id) {
        ProcessDefinition def = this.getById(id);
        if (def == null) {
            throw new IllegalArgumentException("流程定义不存在: " + id);
        }
        return def;
    }
}
