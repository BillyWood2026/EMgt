package com.example.processmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.example.processmgt.entity.PolicyDocument;
import com.example.processmgt.service.PolicyDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 制度文档管理（DB持久化）：起草→提交评审→审批→发布→修订
 */
@RestController
@RequestMapping("/api/document")
public class PolicyDocumentController {

    @Autowired
    private PolicyDocumentService policyDocumentService;

    /** 全量列表(支持 category/status/keyword 过滤) */
    @GetMapping
    public ResponseResult<List<PolicyDocument>> list(@RequestParam(required = false) String category,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<PolicyDocument> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(category)) {
            wrapper.eq(PolicyDocument::getCategory, category);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(PolicyDocument::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(PolicyDocument::getTitle, keyword);
        }
        wrapper.orderByDesc(PolicyDocument::getUpdateTime);
        return ResponseResult.success(policyDocumentService.list(wrapper));
    }

    @GetMapping("/list")
    public ResponseResult<List<PolicyDocument>> listAll(@RequestParam(required = false) String category,
                                                        @RequestParam(required = false) String status,
                                                        @RequestParam(required = false) String keyword) {
        return list(category, status, keyword);
    }

    @GetMapping("/page")
    public ResponseResult<IPage<PolicyDocument>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                                      @RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String category,
                                                      @RequestParam(required = false) String status) {
        return ResponseResult.success(policyDocumentService.pageList(pageNum, pageSize, keyword, category, status));
    }

    @GetMapping("/{id}")
    public ResponseResult<PolicyDocument> getById(@PathVariable Long id) {
        PolicyDocument doc = policyDocumentService.getById(id);
        if (doc == null) {
            throw new IllegalArgumentException("制度文档不存在: " + id);
        }
        return ResponseResult.success(doc);
    }

    /** 起草保存 */
    @PostMapping
    public ResponseResult<PolicyDocument> create(@RequestBody PolicyDocument document) {
        return ResponseResult.success(policyDocumentService.createDraft(document));
    }

    @PutMapping("/{id}")
    public ResponseResult<PolicyDocument> update(@PathVariable Long id, @RequestBody PolicyDocument document) {
        PolicyDocument existing = policyDocumentService.getById(id);
        if (existing == null) {
            throw new IllegalArgumentException("制度文档不存在: " + id);
        }
        if (!PolicyDocument.STATUS_DRAFT.equals(existing.getStatus())
                && !"rejected".equals(existing.getStatus())) {
            throw new IllegalStateException("仅草稿/被驳回状态可编辑，当前状态: " + existing.getStatus());
        }
        document.setId(id);
        document.setStatus(existing.getStatus());
        document.setUpdateTime(LocalDateTime.now());
        policyDocumentService.updateById(document);
        return ResponseResult.success(policyDocumentService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(policyDocumentService.removeById(id));
    }

    @PostMapping("/{id}/submit")
    public ResponseResult<PolicyDocument> submit(@PathVariable Long id) {
        return ResponseResult.success(policyDocumentService.submitReview(id));
    }

    @PostMapping("/{id}/approve")
    public ResponseResult<PolicyDocument> approve(@PathVariable Long id,
                                                  @RequestBody(required = false) Map<String, String> body) {
        String comment = body == null ? null : body.get("comment");
        return ResponseResult.success(policyDocumentService.approve(id, comment));
    }

    @PostMapping("/{id}/reject")
    public ResponseResult<PolicyDocument> reject(@PathVariable Long id,
                                                 @RequestBody(required = false) Map<String, String> body) {
        String comment = body == null ? null : body.get("comment");
        return ResponseResult.success(policyDocumentService.reject(id, comment));
    }

    @PostMapping("/{id}/publish")
    public ResponseResult<PolicyDocument> publish(@PathVariable Long id) {
        return ResponseResult.success(policyDocumentService.publish(id));
    }

    @PostMapping("/{id}/revise")
    public ResponseResult<PolicyDocument> revise(@PathVariable Long id) {
        return ResponseResult.success(policyDocumentService.revise(id));
    }
}
