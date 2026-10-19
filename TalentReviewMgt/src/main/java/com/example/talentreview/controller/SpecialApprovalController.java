package com.example.talentreview.controller;

import com.example.common.dto.ResponseResult;
import com.example.talentreview.entity.SpecialApproval;
import com.example.talentreview.service.SpecialApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 特殊审批 / 人才评审 Controller
 * 对齐页面: talent-devep/Review.vue
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/evaluation/special-approval")
public class SpecialApprovalController {

    @Autowired
    private SpecialApprovalService specialApprovalService;

    /**
     * 按状态分页标签查询：PENDING / IN_PROGRESS / APPROVED / REJECTED。
     * 对任何无法识别的 {status} 字符串保持宽容，返回全部记录。
     */
    @GetMapping("/status/{status}")
    public ResponseResult<List<SpecialApproval>> listByStatus(@PathVariable("status") String status) {
        return ResponseResult.success(specialApprovalService.listByStatus(status));
    }

    /**
     * 新建审批/评审。body 带 id 或已存在的 approvalId 时执行更新（幂等 upsert）。
     */
    @PostMapping
    public ResponseResult<SpecialApproval> create(@RequestBody SpecialApproval body) {
        return ResponseResult.success(specialApprovalService.saveOrUpdateByBusinessKey(body));
    }

    /**
     * 编辑审批/评审（id 在 body 中；前端亦可能只提交 approvalId 作为定位键）。
     */
    @PutMapping
    public ResponseResult<SpecialApproval> update(@RequestBody SpecialApproval body) {
        return ResponseResult.success(specialApprovalService.saveOrUpdateByBusinessKey(body));
    }
}
