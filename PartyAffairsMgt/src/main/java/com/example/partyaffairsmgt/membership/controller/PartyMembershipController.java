package com.example.partyaffairsmgt.membership.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.membership.entity.MembershipChange;
import com.example.partyaffairsmgt.membership.entity.MembershipProof;
import com.example.partyaffairsmgt.membership.service.MembershipService;
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

/** 党籍管理（需求 5.1）。 */
@RestController
@RequestMapping("/api/party/membership")
@CrossOrigin(origins = "*")
public class PartyMembershipController {

    @Autowired
    private MembershipService membershipService;

    @GetMapping("/dict")
    public ResponseResult<Map<String, Object>> dict() {
        return ResponseResult.success(membershipService.dict());
    }

    @GetMapping("/ledger")
    public ResponseResult<List<Map<String, Object>>> ledger(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ResponseResult.success(membershipService.ledger(orgId, status, keyword));
    }

    @GetMapping("/changes")
    public ResponseResult<List<MembershipChange>> changes(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long orgId) {
        return ResponseResult.success(membershipService.changes(memberId, orgId));
    }

    @PostMapping("/change")
    public ResponseResult<MembershipChange> createChange(@RequestBody MembershipChange body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(membershipService.createChange(body));
    }

    @PostMapping("/change/{id}/audit")
    public ResponseResult<MembershipChange> audit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R03);
        boolean pass = Boolean.TRUE.equals(body.get("pass"));
        String opinion = body.get("opinion") == null ? null : body.get("opinion").toString();
        return ResponseResult.success(membershipService.audit(id, pass, opinion));
    }

    @GetMapping("/proof/list")
    public ResponseResult<List<MembershipProof>> proofList(@RequestParam(required = false) Long memberId) {
        return ResponseResult.success(membershipService.proofList(memberId));
    }

    @PostMapping("/proof")
    public ResponseResult<MembershipProof> issueProof(@RequestBody MembershipProof body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(membershipService.issueProof(body));
    }
}
