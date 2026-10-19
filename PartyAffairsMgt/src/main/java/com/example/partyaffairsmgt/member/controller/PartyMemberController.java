package com.example.partyaffairsmgt.member.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.member.entity.MemberArchiveMaterial;
import com.example.partyaffairsmgt.member.entity.MemberSensitiveUnlock;
import com.example.partyaffairsmgt.member.entity.PartyMember;
import com.example.partyaffairsmgt.member.service.MemberProfileService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 党员信息档案控制器（需求 6.1）。
 */
@RestController
@RequestMapping("/api/party/member/info")
@CrossOrigin(origins = "*")
public class PartyMemberController {

    @Autowired
    private MemberProfileService memberProfileService;

    /** 党员列表 */
    @GetMapping("/list")
    public ResponseResult<List<Map<String, Object>>> list(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String politicalStatus,
            @RequestParam(required = false) String post,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ResponseResult.success(memberProfileService.list(orgId, politicalStatus, post, status, keyword));
    }

    /** 党员详情 */
    @GetMapping("/{id}")
    public ResponseResult<Map<String, Object>> detail(@PathVariable Long id) {
        return ResponseResult.success(memberProfileService.detail(id));
    }

    /** 新增/编辑党员 */
    @PostMapping
    public ResponseResult<PartyMember> save(@RequestBody PartyMember member) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(memberProfileService.save(member));
    }

    /** 敏感字段解锁申请 */
    @PostMapping("/{id}/unlock")
    public ResponseResult<MemberSensitiveUnlock> applyUnlock(
            @PathVariable Long id,
            @RequestParam String fieldName,
            @RequestParam String reason) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07);
        return ResponseResult.success(memberProfileService.applyUnlock(id, fieldName, reason));
    }

    /** 敏感字段解锁审批 */
    @PostMapping("/unlock/{id}/audit")
    public ResponseResult<Void> auditUnlock(
            @PathVariable Long id,
            @RequestParam boolean pass,
            @RequestParam(required = false) String opinion) {
        PartyRole.requireAny(PartyRole.R03);
        memberProfileService.auditUnlock(id, pass, opinion);
        return ResponseResult.success(null);
    }

    /** 档案材料列表 */
    @GetMapping("/materials")
    public ResponseResult<List<MemberArchiveMaterial>> materials(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) String materialType) {
        return ResponseResult.success(memberProfileService.materials(memberId, materialType));
    }

    /** 上传档案材料 */
    @PostMapping("/materials")
    public ResponseResult<MemberArchiveMaterial> uploadMaterial(@RequestBody MemberArchiveMaterial material) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(memberProfileService.uploadMaterial(material));
    }

    /** 借阅登记 */
    @PostMapping("/materials/{id}/borrow")
    public ResponseResult<Void> borrowMaterial(@PathVariable Long id, @RequestParam String borrower) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        memberProfileService.borrowMaterial(id, borrower);
        return ResponseResult.success(null);
    }

    /** 归还登记 */
    @PostMapping("/materials/{id}/return")
    public ResponseResult<Void> returnMaterial(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        memberProfileService.returnMaterial(id);
        return ResponseResult.success(null);
    }

    /** 结构统计 */
    @GetMapping("/statistics")
    public ResponseResult<Map<String, Object>> statistics(@RequestParam(required = false) Long orgId) {
        return ResponseResult.success(memberProfileService.statistics(orgId));
    }

    /** 导入预览 */
    @PostMapping("/import/preview")
    public ResponseResult<List<Map<String, Object>>> importPreview(@RequestBody List<Map<String, String>> rows) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(memberProfileService.importPreview(rows));
    }

    /** 导入提交 */
    @PostMapping("/import/submit")
    public ResponseResult<Map<String, Object>> importSubmit(@RequestBody List<Map<String, String>> rows) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(memberProfileService.importSubmit(rows));
    }
}
