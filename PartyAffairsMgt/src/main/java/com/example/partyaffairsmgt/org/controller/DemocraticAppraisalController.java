package com.example.partyaffairsmgt.org.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.org.entity.AppraisalItem;
import com.example.partyaffairsmgt.org.entity.AppraisalPlan;
import com.example.partyaffairsmgt.org.service.DemocraticAppraisalService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 4.4 民主评议党员接口。 */
@RestController
@RequestMapping("/api/party/org/democratic-appraisal")
@CrossOrigin(origins = "*")
public class DemocraticAppraisalController {

    @Autowired private DemocraticAppraisalService service;

    @GetMapping("/plan/list")
    public ResponseResult<List<AppraisalPlan>> plans(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer year) {
        return ResponseResult.success(service.plans(orgId, year));
    }

    @PostMapping("/plan")
    public ResponseResult<AppraisalPlan> savePlan(@RequestBody AppraisalPlan plan) {
        PartyRole.requireAny(PartyRole.R02);
        return ResponseResult.success(service.savePlan(plan));
    }

    @PutMapping("/plan")
    public ResponseResult<AppraisalPlan> updatePlan(@RequestBody AppraisalPlan plan) {
        PartyRole.requireAny(PartyRole.R02);
        return ResponseResult.success(service.savePlan(plan));
    }

    @PostMapping("/plan/{id}/start")
    public ResponseResult<AppraisalPlan> start(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07);
        return ResponseResult.success(service.start(id));
    }

    @GetMapping("/plan/{id}/items")
    public ResponseResult<List<AppraisalItem>> items(@PathVariable Long id) {
        return ResponseResult.success(service.items(id));
    }

    @GetMapping("/plan/{id}/progress")
    public ResponseResult<Map<String, Object>> progress(@PathVariable Long id) {
        return ResponseResult.success(service.progress(id));
    }

    @PostMapping("/plan/{id}/submit-branch")
    public ResponseResult<Map<String, Object>> submitBranch(@PathVariable Long id,
                                                            @RequestBody(required = false) Map<String, String> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07);
        String note = body == null ? null : body.get("overrideNote");
        return ResponseResult.success(service.submitBranch(id, note));
    }

    @PostMapping("/plan/{id}/publish")
    public ResponseResult<AppraisalPlan> publish(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07);
        return ResponseResult.success(service.publish(id));
    }

    @PostMapping("/plan/{id}/feedback")
    public ResponseResult<AppraisalPlan> feedback(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07);
        return ResponseResult.success(service.feedback(id));
    }

    @PostMapping("/item/{id}/self")
    public ResponseResult<AppraisalItem> self(@PathVariable Long id,
                                              @RequestBody Map<String, Object> body) {
        return ResponseResult.success(service.saveSelf(id,
                (String) body.get("selfContent"),
                body.get("participated") == null ? null : Integer.valueOf(body.get("participated").toString())));
    }

    @PostMapping("/item/{id}/scores")
    public ResponseResult<AppraisalItem> scores(@PathVariable Long id,
                                                @RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07);
        return ResponseResult.success(service.saveScores(id,
                dec(body.get("scorePolitics")), dec(body.get("scoreDiscipline")),
                dec(body.get("scoreRole")), dec(body.get("scoreMorality"))));
    }

    @PostMapping("/item/{id}/branch")
    public ResponseResult<AppraisalItem> branch(@PathVariable Long id,
                                                @RequestBody Map<String, String> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07);
        return ResponseResult.success(service.saveBranch(id,
                body.get("branchOpinion"), body.get("result")));
    }

    @PostMapping("/item/{id}/party")
    public ResponseResult<AppraisalItem> party(@PathVariable Long id,
                                               @RequestBody Map<String, String> body) {
        PartyRole.requireAny(PartyRole.R03);
        return ResponseResult.success(service.savePartyOpinion(id, body.get("partyOpinion")));
    }

    @PostMapping("/item/{id}/adjust")
    public ResponseResult<AppraisalItem> adjust(@PathVariable Long id,
                                                @RequestBody Map<String, String> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07);
        return ResponseResult.success(service.requestAdjust(id,
                body.get("result"), body.get("reason")));
    }

    @PostMapping("/item/{id}/adjust-audit")
    public ResponseResult<AppraisalItem> adjustAudit(@PathVariable Long id,
                                                     @RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R03);
        boolean pass = body.get("pass") != null && Boolean.parseBoolean(String.valueOf(body.get("pass")));
        return ResponseResult.success(service.auditAdjust(id, pass, (String) body.get("opinion")));
    }

    private BigDecimal dec(Object o) {
        return o == null || o.toString().isBlank() ? null : new BigDecimal(o.toString());
    }
}
