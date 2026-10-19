package com.example.partyaffairsmgt.activity.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.activity.entity.ActivityPlan;
import com.example.partyaffairsmgt.activity.service.ActivityPlanService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 活动计划控制器（需求 9.1：编制/频次校验/冲突提示/审批；9.2：发布）。 */
@RestController
@RequestMapping("/api/party/activity/plan")
@CrossOrigin(origins = "*")
public class ActivityPlanController {

    @Autowired
    private ActivityPlanService service;

    @GetMapping("/list")
    public ResponseResult<List<ActivityPlan>> list(@RequestParam(required = false) Long orgId,
                                                   @RequestParam(required = false) String activityType,
                                                   @RequestParam(required = false) String approveStatus,
                                                   @RequestParam(required = false) String publishStatus,
                                                   @RequestParam(required = false) Integer year) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(service.list(orgId, activityType, approveStatus, publishStatus, year));
    }

    @GetMapping("/{id}")
    public ResponseResult<ActivityPlan> detail(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(service.detail(id));
    }

    @PostMapping
    public ResponseResult<ActivityPlan> save(@RequestBody ActivityPlan plan) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(service.save(plan));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07);
        service.delete(id);
        return ResponseResult.success(null);
    }

    /** 频次合规校验（缺失抛 E-AC-01 预警） */
    @PostMapping("/check-frequency")
    public ResponseResult<Map<String, Object>> checkFrequency(@RequestParam Long orgId,
                                                              @RequestParam Integer year,
                                                              @RequestParam Integer quarter) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(service.checkFrequency(orgId, year, quarter));
    }

    /** 时间冲突检查（同日重复三会抛 E-AC-02） */
    @GetMapping("/check-conflict")
    public ResponseResult<List<ActivityPlan>> checkConflict(@RequestParam Long orgId,
                                                            @RequestParam String date) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(service.checkConflict(orgId, LocalDate.parse(date)));
    }

    /** 上级党委组织部门审批/备案 */
    @PostMapping("/{id}/approve")
    public ResponseResult<ActivityPlan> approve(@PathVariable Long id,
                                                @RequestParam boolean passed,
                                                @RequestParam(required = false) String opinion) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04);
        return ResponseResult.success(service.approve(id, passed, opinion));
    }

    /** 发布（可定时） */
    @PostMapping("/{id}/publish")
    public ResponseResult<ActivityPlan> publish(@PathVariable Long id,
                                                @RequestParam(required = false) String scheduled) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07, PartyRole.R08);
        LocalDateTime s = scheduled == null || scheduled.isBlank() ? null : LocalDateTime.parse(scheduled);
        return ResponseResult.success(service.publish(id, s));
    }
}
