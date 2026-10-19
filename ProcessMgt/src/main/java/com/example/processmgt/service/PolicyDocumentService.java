package com.example.processmgt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.processmgt.entity.PolicyDocument;

/**
 * 制度文档 Service
 */
public interface PolicyDocumentService extends IService<PolicyDocument> {

    IPage<PolicyDocument> pageList(Integer pageNum, Integer pageSize, String keyword,
                                   String category, String status);

    PolicyDocument createDraft(PolicyDocument document);

    PolicyDocument submitReview(Long id);

    PolicyDocument approve(Long id, String comment);

    PolicyDocument reject(Long id, String comment);

    PolicyDocument publish(Long id);

    /** 修订：基于现行制度派生新版本草稿 */
    PolicyDocument revise(Long id);
}
