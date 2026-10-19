package com.example.partyaffairsmgt.opinion.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.opinion.entity.OpinionHandle;
import com.example.partyaffairsmgt.opinion.entity.OpinionHandleRecord;
import com.example.partyaffairsmgt.opinion.service.OpinionHandleService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 舆情处置流程控制器（需求 8.4：七环节闭环/时限看板/升降级/超时升级）。 */
@RestController
@RequestMapping("/api/party/opinion/handle")
@CrossOrigin(origins = "*")
public class OpinionHandleController {

    @Autowired
    private OpinionHandleService service;

    @GetMapping("/list")
    public ResponseResult<List<OpinionHandle>> list(@RequestParam(required = false) String level,
                                                    @RequestParam(required = false) String flowStatus,
                                                    @RequestParam(required = false) String keyword) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.list(level, flowStatus, keyword));
    }

    @GetMapping("/{id}")
    public ResponseResult<OpinionHandle> detail(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.detail(id));
    }

    @GetMapping("/records/{handleId}")
    public ResponseResult<List<OpinionHandleRecord>> records(@PathVariable Long handleId) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.records(handleId));
    }

    @GetMapping("/dashboard")
    public ResponseResult<Map<String, Object>> dashboard() {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.dashboard());
    }

    @PostMapping("/create/{alertId}")
    public ResponseResult<OpinionHandle> create(@PathVariable Long alertId) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.create(alertId));
    }

    @PostMapping("/{id}/research")
    public ResponseResult<OpinionHandle> research(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.research(id,
                (String) body.get("conclusion"),
                (String) body.get("impact"),
                (String) body.get("riskLevel"),
                body.get("emergency") == null ? null : Integer.valueOf(body.get("emergency").toString())));
    }

    @PostMapping("/{id}/transfer")
    public ResponseResult<OpinionHandle> transfer(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        LocalDateTime deadline = body.get("deadline") == null ? null
                : LocalDateTime.parse(body.get("deadline").toString(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return ResponseResult.success(service.transfer(id,
                (String) body.get("unit"), (String) body.get("owner"), deadline));
    }

    @PostMapping("/{id}/claim")
    public ResponseResult<OpinionHandle> claim(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.claim(id));
    }

    @PostMapping("/{id}/handle")
    public ResponseResult<OpinionHandle> doHandle(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R14);
        return ResponseResult.success(service.doHandle(id, (String) body.get("measure")));
    }

    @PostMapping("/{id}/feedback")
    public ResponseResult<OpinionHandle> feedback(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R14);
        return ResponseResult.success(service.feedback(id,
                (String) body.get("result"), (String) body.get("feedbackFile")));
    }

    @PostMapping("/{id}/review")
    public ResponseResult<OpinionHandle> review(@PathVariable Long id, @RequestParam boolean passed,
                                                @RequestParam(required = false) String opinion) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03);
        return ResponseResult.success(service.review(id, passed, opinion));
    }

    @PostMapping("/{id}/archive")
    public ResponseResult<OpinionHandle> archive(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03);
        return ResponseResult.success(service.archive(id));
    }

    /** 舆情升级/降级（须有研判记录 E-OP-07） */
    @PostMapping("/{id}/change-level")
    public ResponseResult<OpinionHandle> changeLevel(@PathVariable Long id, @RequestParam String newLevel) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(service.changeLevel(id, newLevel));
    }
}
