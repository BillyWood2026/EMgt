package com.example.processmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.dto.ResponseResult;
import com.example.processmgt.entity.PolicyDocument;
import com.example.processmgt.service.PolicyDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * /api/doc 别名（vite 代理 /api/doc -> 8143）
 */
@RestController
@RequestMapping("/api/doc")
public class PolicyDocAliasController {

    @Autowired
    private PolicyDocumentService policyDocumentService;

    @GetMapping
    public ResponseResult<List<PolicyDocument>> list(@RequestParam(required = false) String category,
                                                     @RequestParam(required = false) String status) {
        LambdaQueryWrapper<PolicyDocument> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            wrapper.eq(PolicyDocument::getCategory, category);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PolicyDocument::getStatus, status);
        }
        wrapper.orderByDesc(PolicyDocument::getUpdateTime);
        return ResponseResult.success(policyDocumentService.list(wrapper));
    }
}
