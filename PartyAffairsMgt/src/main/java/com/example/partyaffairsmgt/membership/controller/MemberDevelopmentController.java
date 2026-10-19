package com.example.partyaffairsmgt.membership.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.membership.entity.DevPerson;
import com.example.partyaffairsmgt.membership.entity.DevPlan;
import com.example.partyaffairsmgt.membership.service.MemberDevelopmentService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 发展党员五阶段全流程（需求 5.2）。 */
@RestController
@RequestMapping("/api/party/membership/development")
@CrossOrigin(origins = "*")
public class MemberDevelopmentController {

    @Autowired
    private MemberDevelopmentService service;

    @GetMapping("/dict")
    public ResponseResult<Map<String, Object>> dict() {
        return ResponseResult.success(service.dict());
    }

    // ---------- 计划 ----------

    @GetMapping("/plan/list")
    public ResponseResult<List<DevPlan>> planList(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer year) {
        return ResponseResult.success(service.planList(orgId, year));
    }

    @PostMapping("/plan")
    public ResponseResult<DevPlan> savePlan(@RequestBody DevPlan body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(service.savePlan(body));
    }

    // ---------- 人员 ----------

    @GetMapping("/person/list")
    public ResponseResult<List<DevPerson>> personList(
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return ResponseResult.success(service.personList(stage, orgId, keyword, status));
    }

    @PostMapping("/person")
    public ResponseResult<DevPerson> createPerson(@RequestBody DevPerson body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(service.createPerson(body));
    }

    @GetMapping("/person/{id}")
    public ResponseResult<Map<String, Object>> detail(@PathVariable Long id) {
        return ResponseResult.success(service.detail(id));
    }

    @PostMapping("/person/{id}/node/{nodeKey}")
    public ResponseResult<Map<String, Object>> node(
            @PathVariable Long id, @PathVariable String nodeKey, @RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(service.completeNode(id, nodeKey, body));
    }

    @PostMapping("/person/{id}/action/{action}")
    public ResponseResult<DevPerson> sideAction(
            @PathVariable Long id, @PathVariable String action, @RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R07);
        return ResponseResult.success(service.sideAction(id, action, body));
    }

    @PostMapping("/node/{id}/audit")
    public ResponseResult<com.example.partyaffairsmgt.membership.entity.DevNode> auditNode(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R03);
        boolean pass = Boolean.TRUE.equals(body.get("pass"));
        String opinion = body.get("opinion") == null ? null : body.get("opinion").toString();
        return ResponseResult.success(service.auditNode(id, pass, opinion));
    }

    // ---------- 看板 ----------

    @GetMapping("/board")
    public ResponseResult<Map<String, Object>> board(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer year) {
        return ResponseResult.success(service.board(orgId, year));
    }

    @GetMapping("/timeouts")
    public ResponseResult<List<Map<String, Object>>> timeouts(@RequestParam(required = false) Long orgId) {
        return ResponseResult.success(service.timeouts(orgId));
    }
}
