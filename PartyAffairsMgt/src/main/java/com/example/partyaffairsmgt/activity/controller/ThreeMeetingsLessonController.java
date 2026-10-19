package com.example.partyaffairsmgt.activity.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.activity.entity.ThreeMeetingsLesson;
import com.example.partyaffairsmgt.activity.service.ThreeMeetingsLessonService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 三会一课专项台账控制器（需求 9.4：四本台账/补录/总览/完整率/连续不达标/授课刚性）。 */
@RestController
@RequestMapping("/api/party/activity/ledger")
@CrossOrigin(origins = "*")
public class ThreeMeetingsLessonController {

    @Autowired
    private ThreeMeetingsLessonService service;

    @GetMapping("/list")
    public ResponseResult<List<ThreeMeetingsLesson>> list(@RequestParam(required = false) String meetingType,
                                                          @RequestParam(required = false) Long orgId,
                                                          @RequestParam(required = false) Integer year,
                                                          @RequestParam(required = false) Integer quarter) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(service.list(meetingType, orgId, year, quarter));
    }

    /** 台账补录（追记，原因必填 E-AC-10） */
    @PostMapping("/backfill")
    public ResponseResult<ThreeMeetingsLesson> backfill(@RequestBody ThreeMeetingsLesson l) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(service.backfill(l));
    }

    /** 台账总览（年度/季度，按支部对比） */
    @GetMapping("/overview")
    public ResponseResult<Map<String, Object>> overview(@RequestParam(required = false) Long orgId,
                                                        @RequestParam Integer year,
                                                        @RequestParam(required = false) Integer quarter) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(service.overview(orgId, year, quarter));
    }

    /** 完整率校验（<100% 预警 E-AC-11） */
    @PostMapping("/check-complete")
    public ResponseResult<Map<String, Object>> checkComplete(@RequestParam(required = false) Long orgId,
                                                             @RequestParam Integer year,
                                                             @RequestParam Integer quarter) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(service.checkComplete(orgId, year, quarter));
    }

    /** 连续两季度不达标（升级告警+扣分建议 E-AC-12） */
    @PostMapping("/check-consecutive")
    public ResponseResult<Map<String, Object>> checkConsecutive(@RequestParam(required = false) Long orgId,
                                                                @RequestParam Integer year,
                                                                @RequestParam Integer quarter) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04);
        return ResponseResult.success(service.checkConsecutive(orgId, year, quarter));
    }

    /** 党课授课刚性校验（E-AC-15） */
    @PostMapping("/check-lecturer")
    public ResponseResult<List<Map<String, Object>>> checkLecturer(@RequestParam(required = false) Long orgId,
                                                                   @RequestParam Integer year) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(service.checkLecturerRigid(orgId, year));
    }
}
