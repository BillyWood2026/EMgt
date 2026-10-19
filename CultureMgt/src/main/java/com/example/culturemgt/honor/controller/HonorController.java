package com.example.culturemgt.honor.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.honor.entity.HonorApply;
import com.example.culturemgt.honor.service.HonorService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 荣誉申报与评审公示表彰。 */
@RestController
@RequestMapping("/api/culture/honor")
public class HonorController {

    @Autowired private HonorService service;

    /** 榜样风采广场：终审表彰记录，全员可见。 */
    @GetMapping("/square")
    public ResponseResult<?> square(@RequestParam(required = false) String keyword) {
        return ResponseResult.success(service.square(keyword));
    }

    /** 公示中列表（全员可查看并可提异议）。 */
    @GetMapping("/publicity")
    public ResponseResult<?> publicity() {
        return ResponseResult.success(service.publicity());
    }

    @GetMapping("/mine")
    public ResponseResult<?> mine() {
        return ResponseResult.success(service.mine());
    }

    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String status,
                                  @RequestParam(required = false) Long projectId) {
        return ResponseResult.success(service.list(status, projectId));
    }

    @GetMapping("/{id}")
    public ResponseResult<?> get(@PathVariable Long id) {
        return ResponseResult.success(service.get(id));
    }

    @PostMapping
    public ResponseResult<?> create(@RequestBody HonorApply a) {
        a.setId(null);
        return ResponseResult.success(service.save(a));
    }

    @PutMapping("/{id}")
    public ResponseResult<?> update(@PathVariable Long id, @RequestBody HonorApply a) {
        a.setId(id);
        return ResponseResult.success(service.save(a));
    }

    @PostMapping("/{id}/submit")
    public ResponseResult<?> submit(@PathVariable Long id) {
        service.submit(id);
        return ResponseResult.success("已提交申报");
    }

    /** 部门负责人初审（pass=false 为驳回）。 */
    @PostMapping("/{id}/first-review")
    public ResponseResult<?> firstReview(@PathVariable Long id,
                                         @RequestParam boolean pass,
                                         @RequestParam(required = false) String opinion) {
        service.firstReview(id, pass, opinion);
        return ResponseResult.success(pass ? "初审通过" : "已驳回");
    }

    /** 文化专员复核（pass 时自动读取文化综合得分）。 */
    @PostMapping("/{id}/review")
    public ResponseResult<?> review(@PathVariable Long id,
                                    @RequestParam boolean pass,
                                    @RequestParam(required = false) String opinion) {
        service.review(id, pass, opinion);
        return ResponseResult.success(pass ? "复核通过" : "已驳回");
    }

    /** 发起公示。 */
    @PostMapping("/{id}/publicize")
    public ResponseResult<?> publicize(@PathVariable Long id,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        service.publicize(id, start, end);
        return ResponseResult.success("已发起公示");
    }

    /** 公示期异议申诉，body {reason}。 */
    @PostMapping("/{id}/objection")
    public ResponseResult<?> objection(@PathVariable Long id, @RequestBody Map<String, String> body) {
        service.objection(id, body.get("reason"));
        return ResponseResult.success("异议已提交");
    }

    /** 高管终审表彰，body {awardTime:"yyyy-MM-dd", certFiles:[...], syncCase:true|false}。 */
    @PostMapping("/{id}/finalize")
    public ResponseResult<?> finalize(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        LocalDate awardTime = body.get("awardTime") == null ? null : LocalDate.parse(body.get("awardTime").toString());
        String certFiles = body.get("certFiles") == null ? null : body.get("certFiles").toString();
        boolean syncCase = Boolean.TRUE.equals(body.get("syncCase")) || "true".equals(body.get("syncCase"));
        service.finalize(id, awardTime, certFiles, syncCase);
        return ResponseResult.success("已终审表彰");
    }

    /** 评审驳回（按当前状态鉴权）。 */
    @PostMapping("/{id}/reject")
    public ResponseResult<?> reject(@PathVariable Long id, @RequestParam String opinion) {
        service.reject(id, opinion);
        return ResponseResult.success("已驳回");
    }

    /** 评审记录时间线数据（详情抽屉用，返回原记录含 approval_log/objections JSON）。 */
    @GetMapping("/{id}/timeline")
    public ResponseResult<?> timeline(@PathVariable Long id) {
        HonorApply a = service.get(id);
        return ResponseResult.success(List.of(
                Map.of("approvalLog", a.getApprovalLog() == null ? "[]" : a.getApprovalLog()),
                Map.of("objections", a.getObjections() == null ? "[]" : a.getObjections())
        ));
    }
}
