package com.example.partyaffairsmgt.member.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.member.entity.FloatCert;
import com.example.partyaffairsmgt.member.entity.FloatContact;
import com.example.partyaffairsmgt.member.entity.FloatingMember;
import com.example.partyaffairsmgt.member.service.FloatingMemberService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 流动党员控制器（需求 6.4）。
 */
@RestController
@RequestMapping("/api/party/member/floating")
@CrossOrigin(origins = "*")
public class FloatingMemberController {

    @Autowired
    private FloatingMemberService floatingService;

    /** 流动党员台账 */
    @GetMapping("/list")
    public ResponseResult<List<FloatingMember>> list(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String flowType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ResponseResult.success(floatingService.list(orgId, flowType, status, keyword));
    }

    /** 流出登记 */
    @PostMapping("/out")
    public ResponseResult<FloatingMember> registerOut(@RequestBody FloatingMember floating) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(floatingService.registerOut(floating));
    }

    /** 流入登记 */
    @PostMapping("/in")
    public ResponseResult<FloatingMember> registerIn(@RequestBody FloatingMember floating) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(floatingService.registerIn(floating));
    }

    /** 返回销号 */
    @PostMapping("/{id}/return")
    public ResponseResult<Void> returnBack(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        floatingService.returnBack(id);
        return ResponseResult.success(null);
    }

    /** 双向联系记录 */
    @GetMapping("/{id}/contacts")
    public ResponseResult<List<FloatContact>> contacts(@PathVariable Long id) {
        return ResponseResult.success(floatingService.contacts(id));
    }

    /** 添加联系记录 */
    @PostMapping("/contacts")
    public ResponseResult<FloatContact> addContact(@RequestBody FloatContact contact) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(floatingService.addContact(contact));
    }

    /** 活动证列表 */
    @GetMapping("/certs")
    public ResponseResult<List<FloatCert>> certs(@RequestParam(required = false) String status) {
        return ResponseResult.success(floatingService.certs(status));
    }

    /** 发放活动证 */
    @PostMapping("/{id}/cert")
    public ResponseResult<FloatCert> issueCert(@PathVariable Long id, @RequestParam String certNo) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(floatingService.issueCert(id, certNo));
    }

    /** 注销活动证 */
    @PostMapping("/certs/{id}/revoke")
    public ResponseResult<Void> revokeCert(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        floatingService.revokeCert(id);
        return ResponseResult.success(null);
    }

    /** 失联预警 */
    @GetMapping("/alerts")
    public ResponseResult<List<Map<String, Object>>> alerts() {
        return ResponseResult.success(floatingService.alerts());
    }
}
