package com.example.partyaffairsmgt.assessment.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.assessment.entity.AssessRectify;
import com.example.partyaffairsmgt.assessment.service.AssessRectifyService;
import com.example.partyaffairsmgt.assessment.service.AssessTaskService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 结果应用（需求 10.5）：发布排名、整改台账。
 */
@RestController
@RequestMapping("/api/party/assessment/result")
@CrossOrigin(origins = "*")
public class AssessResultController {

    @Autowired
    private AssessTaskService taskService;
    @Autowired
    private AssessRectifyService rectifyService;

    @PostMapping("/{schemeId}/publish")
    public ResponseResult<Map<String, Object>> publishResults(@PathVariable Long schemeId,
                                                               @RequestParam(required = false, defaultValue = "false") boolean force) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04);
        return ResponseResult.success(taskService.publishResults(schemeId, force));
    }

    @GetMapping("/rectify/list")
    public ResponseResult<List<AssessRectify>> rectifyList(@RequestParam(required = false) Long taskId,
                                                           @RequestParam(required = false) String status) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(rectifyService.list(taskId, status));
    }

    @PostMapping("/rectify/add")
    public ResponseResult<AssessRectify> addRectify(@RequestBody AssessRectify r) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(rectifyService.add(r));
    }

    @PostMapping("/rectify/{id}/finish")
    public ResponseResult<AssessRectify> finishRectify(@PathVariable Long id, @RequestParam String proof) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07);
        return ResponseResult.success(rectifyService.finish(id, proof));
    }

    @PostMapping("/rectify/{id}/close")
    public ResponseResult<AssessRectify> closeRectify(@PathVariable Long id, @RequestParam String closeBy) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(rectifyService.close(id, closeBy));
    }

    @PostMapping("/rectify/overdue-check")
    public ResponseResult<Map<String, Object>> overdueCheck() {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(Map.of("overdue", rectifyService.overdueCheck()));
    }
}
