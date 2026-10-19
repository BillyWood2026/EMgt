package com.example.partyaffairsmgt.activity.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.activity.entity.ActivityEnrollment;
import com.example.partyaffairsmgt.activity.service.ActivityEnrollmentService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 活动发布与报名签到控制器（需求 9.2）。 */
@RestController
@RequestMapping("/api/party/activity/enrollment")
@CrossOrigin(origins = "*")
public class ActivityEnrollmentController {

    @Autowired
    private ActivityEnrollmentService service;

    @GetMapping("/list")
    public ResponseResult<List<ActivityEnrollment>> list(@RequestParam(required = false) Long activityId,
                                                        @RequestParam(required = false) String signStatus,
                                                        @RequestParam(required = false) String memberName) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(service.list(activityId, signStatus, memberName));
    }

    @PostMapping("/enroll")
    public ResponseResult<ActivityEnrollment> enroll(@RequestParam Long activityId,
                                                     @RequestParam Long memberId,
                                                     @RequestParam String memberName) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(service.enroll(activityId, memberId, memberName));
    }

    /** 签到（force=true 跳过活动当天校验用于演示；定位超半径 E-AC-06 标记待核实） */
    @PostMapping("/sign")
    public ResponseResult<ActivityEnrollment> sign(@RequestParam Long activityId,
                                                   @RequestParam Long memberId,
                                                   @RequestParam(defaultValue = "现场扫码") String method,
                                                   @RequestParam(required = false) Integer distanceM,
                                                   @RequestParam(defaultValue = "false") boolean force) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(service.sign(activityId, memberId, method, distanceM, force));
    }

    /** 离线补签（原因必填 E-AC-05） */
    @PostMapping("/{id}/makeup")
    public ResponseResult<ActivityEnrollment> makeup(@PathVariable Long id,
                                                     @RequestParam String reason,
                                                     @RequestParam(defaultValue = "false") boolean directPass) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(service.makeup(id, reason, directPass));
    }

    /** 补签审批 */
    @PostMapping("/{id}/audit")
    public ResponseResult<ActivityEnrollment> audit(@PathVariable Long id, @RequestParam boolean pass) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07);
        return ResponseResult.success(service.auditMakeup(id, pass));
    }

    @GetMapping("/stat/{activityId}")
    public ResponseResult<Map<String, Object>> stat(@PathVariable Long activityId) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(service.stat(activityId));
    }
}
