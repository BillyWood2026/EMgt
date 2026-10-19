package com.example.partyaffairsmgt.activity.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.activity.entity.ActivityRecord;
import com.example.partyaffairsmgt.activity.entity.ActivityResolution;
import com.example.partyaffairsmgt.activity.service.ActivityRecordService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 活动记录与归档控制器（需求 9.3：模板化记录/归档校验/自动归集台账/决议跟踪）。 */
@RestController
@RequestMapping("/api/party/activity/record")
@CrossOrigin(origins = "*")
public class ActivityRecordController {

    @Autowired
    private ActivityRecordService service;

    @GetMapping("/list")
    public ResponseResult<List<ActivityRecord>> list(@RequestParam(required = false) Long orgId,
                                                     @RequestParam(required = false) String activityType,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(required = false) Integer year) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(service.list(orgId, activityType, status, year));
    }

    @GetMapping("/{id}")
    public ResponseResult<ActivityRecord> detail(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(service.detail(id));
    }

    @PostMapping
    public ResponseResult<ActivityRecord> save(@RequestBody ActivityRecord record) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(service.save(record));
    }

    /** 归档（要素缺失 E-AC-08、实到签到不一致 E-AC-09、补录无原因 E-AC-10，归档后自动入台账） */
    @PostMapping("/{id}/archive")
    public ResponseResult<ActivityRecord> archive(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(service.archive(id));
    }

    // ---------------- 决议事项 ----------------

    @GetMapping("/resolution/list/{recordId}")
    public ResponseResult<List<ActivityResolution>> resolutions(@PathVariable Long recordId) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(service.resolutions(recordId));
    }

    @PostMapping("/resolution")
    public ResponseResult<ActivityResolution> saveResolution(@RequestBody ActivityResolution res) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(service.saveResolution(res));
    }

    @PostMapping("/resolution/{id}/finish")
    public ResponseResult<ActivityResolution> finishResolution(@PathVariable Long id,
                                                               @RequestParam(required = false) String remark) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(service.finishResolution(id, remark));
    }

    /** 决议超期检查（E-AC-16） */
    @PostMapping("/resolution/check-overdue")
    public ResponseResult<List<ActivityResolution>> checkOverdue() {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(service.checkOverdue());
    }
}
