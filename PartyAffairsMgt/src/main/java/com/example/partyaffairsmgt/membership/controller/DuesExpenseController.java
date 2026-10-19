package com.example.partyaffairsmgt.membership.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.membership.entity.DuesExpense;
import com.example.partyaffairsmgt.membership.entity.DuesUsePlan;
import com.example.partyaffairsmgt.membership.service.DuesExpenseService;
import java.time.LocalDate;
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

/** 党费使用管理（需求 5.4）。 */
@RestController
@RequestMapping("/api/party/membership/dues-expense")
@CrossOrigin(origins = "*")
public class DuesExpenseController {

    @Autowired
    private DuesExpenseService expenseService;

    @GetMapping("/categories")
    public ResponseResult<List<String>> categories() {
        return ResponseResult.success(expenseService.categories());
    }

    @GetMapping("/plan/list")
    public ResponseResult<List<DuesUsePlan>> planList(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer year) {
        return ResponseResult.success(expenseService.planList(orgId, year));
    }

    @PostMapping("/plan")
    public ResponseResult<DuesUsePlan> savePlan(@RequestBody DuesUsePlan body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(expenseService.savePlan(body));
    }

    @GetMapping("/list")
    public ResponseResult<List<DuesExpense>> list(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String auditStatus) {
        return ResponseResult.success(expenseService.list(orgId, year, auditStatus));
    }

    @PostMapping
    public ResponseResult<Map<String, Object>> create(@RequestBody DuesExpense body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07, PartyRole.R08, PartyRole.R12);
        return ResponseResult.success(expenseService.create(body));
    }

    @PostMapping("/{id}/audit")
    public ResponseResult<DuesExpense> audit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R03);
        boolean pass = Boolean.TRUE.equals(body.get("pass"));
        String opinion = body.get("opinion") == null ? null : body.get("opinion").toString();
        return ResponseResult.success(expenseService.audit(id, pass, opinion));
    }

    @PostMapping("/{id}/register")
    public ResponseResult<DuesExpense> register(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R12);
        LocalDate date = body.get("expenseDate") == null ? null : LocalDate.parse(body.get("expenseDate").toString());
        String result = body.get("result") == null ? null : body.get("result").toString();
        return ResponseResult.success(expenseService.register(id, date, result));
    }

    @GetMapping("/year-summary")
    public ResponseResult<Map<String, Object>> yearSummary(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer year) {
        return ResponseResult.success(expenseService.yearSummary(orgId, year));
    }
}
