package com.example.skillcert.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.example.skillcert.entity.SkillApplication;
import com.example.skillcert.service.SkillApplicationService;
import com.example.skillcert.service.SkillCertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 技能评定/评审（skill-certification/SkillReview.vue + Certification.vue 审批）
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/skill-cert/assessment")
public class SkillAssessmentController {

    @Autowired
    private SkillApplicationService skillApplicationService;
    @Autowired
    private SkillCertificateService skillCertificateService;

    @GetMapping("/list")
    public ResponseResult<IPage<SkillApplication>> list(@RequestParam(defaultValue = "1") long current,
                                                          @RequestParam(defaultValue = "20") long size,
                                                          @RequestParam(required = false) String reviewStatus,
                                                          @RequestParam(required = false) Long planId) {
        return ResponseResult.success(
                skillApplicationService.pageReview(current, size, reviewStatus, planId));
    }

    /** 发起补考 */
    @PostMapping("/retake")
    public ResponseResult<Boolean> retake(@RequestBody Map<String, Object> body) {
        Object id = body.get("id");
        if (id == null) {
            return ResponseResult.error(400, "缺少参数 id");
        }
        return ResponseResult.success(skillApplicationService.retake(Long.valueOf(String.valueOf(id))));
    }

    /** 提交评分 */
    @PostMapping("/score")
    public ResponseResult<Boolean> score(@RequestBody Map<String, Object> body) {
        return ResponseResult.success(skillApplicationService.score(body));
    }

    /** 审批通过（Certification.vue，id 为证书行 id） */
    @PostMapping("/{id}/approve-level")
    public ResponseResult<Boolean> approveLevel(@PathVariable Long id) {
        return ResponseResult.success(skillCertificateService.approve(id));
    }

    /** 审批拒绝（Certification.vue，id 为证书行 id） */
    @PostMapping("/{id}/reject-level")
    public ResponseResult<Boolean> rejectLevel(@PathVariable Long id) {
        return ResponseResult.success(skillCertificateService.reject(id));
    }
}
