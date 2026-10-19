package com.example.orgmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.orgmgt.entity.OrgPersonalizedQuotaConfig;
import com.example.orgmgt.mapper.OrgPersonalizedQuotaConfigMapper;
import com.example.orgmgt.service.OrgPersonalizedQuotaConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Organization Personalized Quota Config Controller
 * REST API for organization-specific quota and span parameter configuration
 */
@Tag(name = "Organization Personalized Quota Config")
@RestController
@RequestMapping({"/api/org-personalized-quota", "/api/personalized-quota"})
public class OrgPersonalizedQuotaConfigController {

    @Autowired
    private OrgPersonalizedQuotaConfigService configService;

    @Autowired
    private OrgPersonalizedQuotaConfigMapper configMapper;

    @Operation(summary = "Query configs as flat list (frontend convention)")
    @GetMapping("/list")
    public ResponseResult<List<OrgPersonalizedQuotaConfig>> getList(
            @RequestParam(required = false) String orgCode,
            @RequestParam(required = false) String positionLevel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        LambdaQueryWrapper<OrgPersonalizedQuotaConfig> wrapper = Wrappers.<OrgPersonalizedQuotaConfig>lambdaQuery()
                .eq(orgCode != null && !orgCode.trim().isEmpty(), OrgPersonalizedQuotaConfig::getOrgCode, orgCode)
                .eq(positionLevel != null && !positionLevel.trim().isEmpty(),
                        OrgPersonalizedQuotaConfig::getPositionLevel, positionLevel)
                .eq(status != null && !status.trim().isEmpty(), OrgPersonalizedQuotaConfig::getStatus, status)
                .eq(orgId != null, OrgPersonalizedQuotaConfig::getOrgId, orgId)
                .orderByDesc(OrgPersonalizedQuotaConfig::getConfigureTime);
        return ResponseResult.success(configMapper.selectList(wrapper));
    }
    
    @Operation(summary = "Query configs with pagination")
    @GetMapping("/page")
    public ResponseResult<Page<OrgPersonalizedQuotaConfig>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String status) {
        Page<OrgPersonalizedQuotaConfig> page = new Page<>(pageNum, pageSize);
        return ResponseResult.success(page);
    }
    
    @Operation(summary = "Query active configs")
    @GetMapping("/active")
    public ResponseResult<List<OrgPersonalizedQuotaConfig>> getActiveConfigs() {
        return ResponseResult.success(configService.getActiveConfigs());
    }
    
    @Operation(summary = "Query config by organization")
    @GetMapping("/org/{orgId}")
    public ResponseResult<OrgPersonalizedQuotaConfig> getByOrgId(@PathVariable Long orgId) {
        return ResponseResult.success(configService.getByOrgId(orgId));
    }
    
    @Operation(summary = "Query config by org and position level")
    @GetMapping("/org/{orgId}/level/{positionLevel}")
    public ResponseResult<OrgPersonalizedQuotaConfig> getByOrgAndLevel(
            @PathVariable Long orgId,
            @PathVariable String positionLevel) {
        return ResponseResult.success(configService.getByOrgAndLevel(orgId, positionLevel));
    }
    
    @Operation(summary = "Get config by ID")
    @GetMapping("/{id}")
    public ResponseResult<OrgPersonalizedQuotaConfig> getById(@PathVariable Long id) {
        return ResponseResult.success(configService.getById(id));
    }
    
    @Operation(summary = "Create new personalized config")
    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody OrgPersonalizedQuotaConfig config) {
        return ResponseResult.success(configService.createConfig(config));
    }
    
    @Operation(summary = "Update personalized config")
    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody OrgPersonalizedQuotaConfig config) {
        config.setId(id);
        return ResponseResult.success(configService.updateConfig(config));
    }
    
    @Operation(summary = "Submit config for approval")
    @PutMapping("/{id}/submit")
    public ResponseResult<Boolean> submit(@PathVariable Long id) {
        return ResponseResult.success(configService.submitForApproval(id));
    }

    @Operation(summary = "Submit config for approval (frontend POST alias)")
    @PostMapping("/{id}/submit")
    public ResponseResult<Boolean> submitPost(@PathVariable Long id) {
        return ResponseResult.success(configService.submitForApproval(id));
    }

    @Operation(summary = "Delete personalized config")
    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> remove(@PathVariable Long id) {
        return ResponseResult.success(configMapper.deleteById(id) > 0);
    }
    
    @Operation(summary = "Approve config")
    @PutMapping("/{id}/approve")
    public ResponseResult<Boolean> approve(
            @PathVariable Long id,
            @RequestParam(required = false) String approver,
            @RequestBody(required = false) Map<String, Object> body) {
        String who = approver;
        if ((who == null || who.trim().isEmpty()) && body != null) {
            Object a = body.get("approver");
            if (a == null) {
                a = body.get("operator");
            }
            who = a == null ? "admin" : String.valueOf(a);
        }
        if (who == null || who.trim().isEmpty()) {
            who = "admin";
        }
        return ResponseResult.success(configService.approveConfig(id, who));
    }

    @Operation(summary = "Reject config")
    @PutMapping("/{id}/reject")
    public ResponseResult<Boolean> reject(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @RequestBody(required = false) Map<String, Object> body) {
        String r = reason;
        if ((r == null || r.trim().isEmpty()) && body != null) {
            Object o = body.get("reason");
            if (o == null) {
                o = body.get("rejectReason");
            }
            r = o == null ? "审批驳回" : String.valueOf(o);
        }
        if (r == null || r.trim().isEmpty()) {
            r = "审批驳回";
        }
        return ResponseResult.success(configService.rejectConfig(id, r));
    }

    @Operation(summary = "Toggle config enabled/disabled")
    @PutMapping("/{id}/toggle")
    public ResponseResult<Boolean> toggle(
            @PathVariable Long id,
            @RequestParam(required = false) Boolean enabled,
            @RequestBody(required = false) Map<String, Object> body) {
        Boolean target = enabled;
        if (target == null && body != null && body.get("enabled") != null) {
            target = Boolean.valueOf(String.valueOf(body.get("enabled")));
        }
        if (target == null) {
            OrgPersonalizedQuotaConfig current = configService.getById(id);
            target = current == null || current.getEnabled() == null ? Boolean.TRUE : !current.getEnabled();
        }
        return ResponseResult.success(configService.toggleConfig(id, target));
    }

    @Operation(summary = "Make config effective (approve + activate)")
    @PutMapping("/{id}/effective")
    public ResponseResult<Boolean> makeEffective(@PathVariable Long id) {
        OrgPersonalizedQuotaConfig config = configService.getById(id);
        if (config == null) {
            return ResponseResult.success(false);
        }
        config.setStatus("ACTIVE");
        config.setEnabled(true);
        return ResponseResult.success(configMapper.updateById(config) > 0);
    }

    @Operation(summary = "Expire a single config")
    @PostMapping("/{id}/expire")
    public ResponseResult<Boolean> expireOne(@PathVariable Long id) {
        OrgPersonalizedQuotaConfig config = configService.getById(id);
        if (config == null) {
            return ResponseResult.success(false);
        }
        config.setStatus("EXPIRED");
        config.setEnabled(false);
        return ResponseResult.success(configMapper.updateById(config) > 0);
    }
    
    @Operation(summary = "Get effective config for validation")
    @GetMapping("/effective")
    public ResponseResult<OrgPersonalizedQuotaConfig> getEffectiveConfig(
            @RequestParam Long orgId,
            @RequestParam String positionLevel) {
        return ResponseResult.success(configService.getEffectiveConfig(orgId, positionLevel));
    }
    
    @Operation(summary = "Expire outdated configs")
    @PostMapping("/expire")
    public ResponseResult<Integer> expireConfigs() {
        return ResponseResult.success(configService.expireConfigs());
    }
}
