package com.example.processmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.processmgt.entity.PolicyDocument;
import com.example.processmgt.mapper.PolicyDocumentMapper;
import com.example.processmgt.service.PolicyDocumentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 制度文档 Service 实现
 */
@Service
public class PolicyDocumentServiceImpl extends ServiceImpl<PolicyDocumentMapper, PolicyDocument>
        implements PolicyDocumentService {

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public IPage<PolicyDocument> pageList(Integer pageNum, Integer pageSize, String keyword,
                                           String category, String status) {
        Page<PolicyDocument> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PolicyDocument> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(PolicyDocument::getTitle, keyword)
                    .or().like(PolicyDocument::getDocNo, keyword));
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(PolicyDocument::getCategory, category);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(PolicyDocument::getStatus, status);
        }
        wrapper.orderByDesc(PolicyDocument::getUpdateTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional
    public PolicyDocument createDraft(PolicyDocument document) {
        if (!StringUtils.hasText(document.getDocNo())) {
            document.setDocNo(generateDocNo());
        }
        if (document.getVersion() == null) {
            document.setVersion(1);
        }
        document.setStatus(PolicyDocument.STATUS_DRAFT);
        LocalDateTime now = LocalDateTime.now();
        document.setCreateTime(now);
        document.setUpdateTime(now);
        this.save(document);
        return document;
    }

    @Override
    @Transactional
    public PolicyDocument submitReview(Long id) {
        PolicyDocument doc = mustGet(id);
        if (!PolicyDocument.STATUS_DRAFT.equals(doc.getStatus())
                && !PolicyDocument.STATUS_DEPRECATED.equals(doc.getStatus())
                && !"rejected".equals(doc.getStatus())) {
            throw new IllegalStateException("仅草稿/被驳回的制度可提交评审，当前状态: " + doc.getStatus());
        }
        doc.setStatus(PolicyDocument.STATUS_PENDING);
        doc.setUpdateTime(LocalDateTime.now());
        this.updateById(doc);
        return doc;
    }

    @Override
    @Transactional
    public PolicyDocument approve(Long id, String comment) {
        PolicyDocument doc = mustGet(id);
        if (!PolicyDocument.STATUS_PENDING.equals(doc.getStatus())) {
            throw new IllegalStateException("仅待评审(pending)制度可审批，当前状态: " + doc.getStatus());
        }
        doc.setStatus(PolicyDocument.STATUS_APPROVED);
        doc.setApprovalComment(comment);
        doc.setUpdateTime(LocalDateTime.now());
        this.updateById(doc);
        return doc;
    }

    @Override
    @Transactional
    public PolicyDocument reject(Long id, String comment) {
        PolicyDocument doc = mustGet(id);
        if (!PolicyDocument.STATUS_PENDING.equals(doc.getStatus())) {
            throw new IllegalStateException("仅待评审(pending)制度可驳回，当前状态: " + doc.getStatus());
        }
        doc.setStatus("rejected");
        doc.setApprovalComment(comment);
        doc.setUpdateTime(LocalDateTime.now());
        this.updateById(doc);
        return doc;
    }

    @Override
    @Transactional
    public PolicyDocument publish(Long id) {
        PolicyDocument doc = mustGet(id);
        if (!PolicyDocument.STATUS_APPROVED.equals(doc.getStatus())) {
            throw new IllegalStateException("仅评审通过(approved)的制度可发布，当前状态: " + doc.getStatus());
        }
        doc.setStatus(PolicyDocument.STATUS_ACTIVE);
        doc.setUpdateTime(LocalDateTime.now());
        this.updateById(doc);
        return doc;
    }

    @Override
    @Transactional
    public PolicyDocument revise(Long id) {
        PolicyDocument source = mustGet(id);
        if (!PolicyDocument.STATUS_ACTIVE.equals(source.getStatus())
                && !PolicyDocument.STATUS_DEPRECATED.equals(source.getStatus())) {
            throw new IllegalStateException("仅已发布(active)制度可修订，当前状态: " + source.getStatus());
        }
        PolicyDocument copy = new PolicyDocument();
        copy.setDocNo(source.getDocNo());
        copy.setTitle(source.getTitle());
        copy.setCategory(source.getCategory());
        copy.setContent(source.getContent());
        copy.setOwnerDept(source.getOwnerDept());
        copy.setApprover(source.getApprover());
        copy.setCreateBy(source.getCreateBy());
        copy.setVersion((source.getVersion() == null ? 1 : source.getVersion()) + 1);
        copy.setStatus(PolicyDocument.STATUS_DRAFT);
        LocalDateTime now = LocalDateTime.now();
        copy.setCreateTime(now);
        copy.setUpdateTime(now);
        this.save(copy);

        // 原版本转为废止，等待新版本发布
        source.setStatus(PolicyDocument.STATUS_DEPRECATED);
        source.setUpdateTime(now);
        this.updateById(source);
        return copy;
    }

    private String generateDocNo() {
        String base = "ZD-" + LocalDateTime.now().format(NO_FMT)
                + String.format("%02d", ThreadLocalRandom.current().nextInt(100));
        return base;
    }

    private PolicyDocument mustGet(Long id) {
        PolicyDocument doc = this.getById(id);
        if (doc == null) {
            throw new IllegalArgumentException("制度文档不存在: " + id);
        }
        return doc;
    }
}
