package com.example.skillcert.controller;

import com.example.common.dto.ResponseResult;
import com.example.skillcert.entity.SkillCertificate;
import com.example.skillcert.service.SkillCertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 证书与年度复审（skill-certification/Certification.vue + SkillCertificate.vue）
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/skill-cert/annual-review")
public class AnnualReviewController {

    @Autowired
    private SkillCertificateService skillCertificateService;

    @GetMapping("/list")
    public ResponseResult<Map<String, Object>> list(@RequestParam(defaultValue = "1") long current,
                                                      @RequestParam(defaultValue = "20") long size,
                                                      @RequestParam(required = false) String employeeName,
                                                      @RequestParam(required = false) String skillName,
                                                      @RequestParam(required = false) String status,
                                                      @RequestParam(required = false) String certStatus,
                                                      @RequestParam(required = false) String certType,
                                                      @RequestParam(required = false) String certLevel,
                                                      @RequestParam(required = false) String keyword) {
        return ResponseResult.success(skillCertificateService.pageList(
                current, size, employeeName, skillName, status, certStatus, certType, certLevel, keyword));
    }

    @GetMapping("/{id}")
    public ResponseResult<SkillCertificate> getById(@PathVariable Long id) {
        return ResponseResult.success(skillCertificateService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody SkillCertificate certificate) {
        return ResponseResult.success(skillCertificateService.save(certificate));
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody SkillCertificate certificate) {
        certificate.setId(id);
        return ResponseResult.success(skillCertificateService.updateById(certificate));
    }

    /** 吊销证书（SkillCertificate.vue） */
    @PutMapping("/{id}/revoke")
    public ResponseResult<Boolean> revoke(@PathVariable Long id) {
        return ResponseResult.success(skillCertificateService.revoke(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(skillCertificateService.removeById(id));
    }

    /** 导出占位（Vue window.open 调用） */
    @GetMapping("/export")
    public ResponseResult<String> export() {
        return ResponseResult.success("证书台账导出任务已提交");
    }
}
