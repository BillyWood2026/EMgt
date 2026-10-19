package com.example.skillcert.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.example.skillcert.entity.SkillWarning;
import com.example.skillcert.service.LedgerService;
import com.example.skillcert.service.SkillWarningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 技能档案与预警（skill-certification/SkillProfile.vue + SkillWarning.vue）
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/skill-cert/ledger")
public class LedgerController {

    @Autowired
    private LedgerService ledgerService;
    @Autowired
    private SkillWarningService skillWarningService;

    /** 预警统计 */
    @GetMapping("/statistics")
    public ResponseResult<Map<String, Object>> statistics() {
        return ResponseResult.success(ledgerService.statistics());
    }

    /** 我的技能档案 */
    @GetMapping("/my")
    public ResponseResult<List<Map<String, Object>>> my() {
        return ResponseResult.success(ledgerService.myProfile());
    }

    /** 员工技能档案详情 */
    @GetMapping("/employee/{id}")
    public ResponseResult<Map<String, Object>> employee(@PathVariable Long id) {
        return ResponseResult.success(ledgerService.employeeProfile(id));
    }

    /** 处理预警 */
    @PutMapping("/{id}/process")
    public ResponseResult<Boolean> process(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseResult.success(skillWarningService.process(id, body));
    }

    /** 预警列表（按 warningType 过滤） */
    @GetMapping("/warning-list")
    public ResponseResult<IPage<SkillWarning>> warningList(@RequestParam(defaultValue = "1") long current,
                                                            @RequestParam(defaultValue = "20") long size,
                                                            @RequestParam(required = false) String warningType,
                                                            @RequestParam(required = false) String orgId,
                                                            @RequestParam(required = false) String skillTypeId,
                                                            @RequestParam(required = false) String keyword) {
        return ResponseResult.success(
                skillWarningService.pageWarnings(current, size, warningType, orgId, skillTypeId, keyword));
    }

    /** 证书即将到期预警（强制 CERT_EXPIRING） */
    @GetMapping("/expiring")
    public ResponseResult<IPage<SkillWarning>> expiring(@RequestParam(defaultValue = "1") long current,
                                                         @RequestParam(defaultValue = "20") long size,
                                                         @RequestParam(required = false) String orgId,
                                                         @RequestParam(required = false) String skillTypeId,
                                                         @RequestParam(required = false) String keyword) {
        return ResponseResult.success(
                skillWarningService.pageWarnings(current, size, "CERT_EXPIRING", orgId, skillTypeId, keyword));
    }
}
