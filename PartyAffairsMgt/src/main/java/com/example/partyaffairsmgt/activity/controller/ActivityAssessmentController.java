package com.example.partyaffairsmgt.activity.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.activity.entity.ActivityAssessment;
import com.example.partyaffairsmgt.activity.service.ActivityAssessmentService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 活动统计与考核控制器（需求 9.5：完成率看板/参与率/未达标/考核生成）。 */
@RestController
@RequestMapping("/api/party/activity/assessment")
@CrossOrigin(origins = "*")
public class ActivityAssessmentController {

    @Autowired
    private ActivityAssessmentService service;

    @GetMapping("/list")
    public ResponseResult<List<ActivityAssessment>> list(@RequestParam(required = false) Long orgId,
                                                         @RequestParam(required = false) String periodType,
                                                         @RequestParam(required = false) Integer year) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(service.list(orgId, periodType, year));
    }

    /** 三会一课完成率/参与率看板 */
    @GetMapping("/dashboard")
    public ResponseResult<Map<String, Object>> dashboard(@RequestParam(required = false) Long orgId,
                                                         @RequestParam Integer year,
                                                         @RequestParam(required = false) Integer quarter) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(service.dashboard(orgId, year, quarter));
    }

    /** 未达标清单 */
    @GetMapping("/unqualified")
    public ResponseResult<List<ActivityAssessment>> unqualified(@RequestParam(required = false) Integer year) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(service.unqualifiedList(year));
    }

    /** 生成考核（数据缺失 E-AC-14；参与率<80% 联动扣分建议） */
    @PostMapping("/generate")
    public ResponseResult<ActivityAssessment> generate(@RequestParam Long orgId,
                                                       @RequestParam String orgName,
                                                       @RequestParam Integer year,
                                                       @RequestParam(required = false) Integer quarter,
                                                       @RequestParam(defaultValue = "0") Integer memberTotal,
                                                       @RequestParam(defaultValue = "false") boolean forceNoData) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(service.generate(orgId, orgName, year, quarter, memberTotal, forceNoData));
    }

    @PostMapping
    public ResponseResult<ActivityAssessment> save(@RequestBody ActivityAssessment a) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(service.save(a));
    }
}
