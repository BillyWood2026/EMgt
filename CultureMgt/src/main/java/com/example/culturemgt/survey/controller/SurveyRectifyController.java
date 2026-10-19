package com.example.culturemgt.survey.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.survey.entity.SurveyRectify;
import com.example.culturemgt.survey.service.RectifyService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 氛围短板整改跟踪（BPM：方案→审核→实施→验收闭环）。 */
@RestController
@RequestMapping("/api/culture/survey-rectify")
public class SurveyRectifyController {

    @Autowired private RectifyService service;

    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String status,
                                  @RequestParam(required = false) String dept,
                                  @RequestParam(required = false) String owner,
                                  @RequestParam(required = false) Boolean overdueOnly) {
        return ResponseResult.success(service.list(status, dept, owner, overdueOnly));
    }

    @GetMapping("/{id}")
    public ResponseResult<?> get(@PathVariable Long id) {
        return ResponseResult.success(service.get(id));
    }

    /** 创建整改任务（文化岗），body 全字段。 */
    @PostMapping
    public ResponseResult<?> create(@RequestBody SurveyRectify body) {
        body.setId(null);
        return ResponseResult.success(service.create(body));
    }

    /** 责任人提交整改方案，body {plan}。 */
    @PostMapping("/{id}/plan")
    public ResponseResult<?> submitPlan(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseResult.success(service.submitPlan(id, body.get("plan")));
    }

    /** 文化专员审核方案，?pass=&opinion=。 */
    @PostMapping("/{id}/plan-review")
    public ResponseResult<?> reviewPlan(@PathVariable Long id,
                                        @RequestParam boolean pass,
                                        @RequestParam String opinion) {
        return ResponseResult.success(service.reviewPlan(id, pass, opinion));
    }

    /** 责任人提交验收材料，body {implementNote, evidence}。 */
    @PostMapping("/{id}/evidence")
    public ResponseResult<?> submitEvidence(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseResult.success(service.submitEvidence(id, body.get("implementNote"), body.get("evidence")));
    }

    /** 文化专员验收，?pass=&opinion=。 */
    @PostMapping("/{id}/accept")
    public ResponseResult<?> reviewAccept(@PathVariable Long id,
                                          @RequestParam boolean pass,
                                          @RequestParam String opinion) {
        return ResponseResult.success(service.reviewAccept(id, pass, opinion));
    }
}
