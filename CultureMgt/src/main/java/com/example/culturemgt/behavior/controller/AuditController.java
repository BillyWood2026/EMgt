package com.example.culturemgt.behavior.controller;

import com.example.culturemgt.behavior.entity.BehaviorAudit;
import com.example.culturemgt.behavior.service.BehaviorAuditService;
import com.example.culturemgt.common.ResponseResult;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 部门行为稽核（登记→告知→确认/申诉→整改→验收→生效）。 */
@RestController
@RequestMapping("/api/culture/behavior/audit")
public class AuditController {

    @Autowired private BehaviorAuditService service;

    /** 后台稽核列表（文化岗）。 */
    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String status,
                                  @RequestParam(required = false) String dept,
                                  @RequestParam(required = false) String targetUser) {
        return ResponseResult.success(service.adminList(status, dept, targetUser));
    }

    /** 我的被稽核记录。 */
    @GetMapping("/mine")
    public ResponseResult<?> mine() {
        return ResponseResult.success(service.myAudit());
    }

    @GetMapping("/{id}")
    public ResponseResult<?> get(@PathVariable Long id) {
        return ResponseResult.success(service.get(id));
    }

    /** 登记稽核（文化岗，状态直接=已告知）。 */
    @PostMapping
    public ResponseResult<?> create(@RequestBody BehaviorAudit body) {
        body.setId(null);
        return ResponseResult.success(service.create(body));
    }

    /** 当事人确认。 */
    @PostMapping("/{id}/confirm")
    public ResponseResult<?> confirm(@PathVariable Long id) {
        service.confirm(id);
        return ResponseResult.success(null);
    }

    /** 当事人申诉，body {reason}。 */
    @PostMapping("/{id}/appeal")
    public ResponseResult<?> appeal(@PathVariable Long id, @RequestBody Map<String, String> body) {
        service.appeal(id, body.get("reason"));
        return ResponseResult.success(null);
    }

    /** 申诉裁定，?uphold=&opinion=（uphold=true 维持原判进入整改，否则申诉成立驳回归档）。 */
    @PostMapping("/{id}/appeal-verdict")
    public ResponseResult<?> verdictAppeal(@PathVariable Long id,
                                           @RequestParam boolean uphold,
                                           @RequestParam String opinion) {
        service.verdictAppeal(id, uphold, opinion);
        return ResponseResult.success(null);
    }

    /** 当事人提交整改材料，body {note, evidence}。 */
    @PostMapping("/{id}/rectify")
    public ResponseResult<?> submitRectify(@PathVariable Long id, @RequestBody Map<String, String> body) {
        service.submitRectify(id, body.get("note"), body.get("evidence"));
        return ResponseResult.success(null);
    }

    /** 验收，?pass=&opinion=（通过→已生效并扣分入账）。 */
    @PostMapping("/{id}/accept")
    public ResponseResult<?> accept(@PathVariable Long id,
                                    @RequestParam boolean pass,
                                    @RequestParam String opinion) {
        service.accept(id, pass, opinion);
        return ResponseResult.success(null);
    }
}
