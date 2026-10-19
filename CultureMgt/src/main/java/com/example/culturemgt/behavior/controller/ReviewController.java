package com.example.culturemgt.behavior.controller;

import com.example.culturemgt.behavior.entity.BehaviorReviewConfig;
import com.example.culturemgt.behavior.entity.BehaviorReviewRecord;
import com.example.culturemgt.behavior.service.ReviewService;
import com.example.culturemgt.common.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 价值观评议（配置 + 评议记录）。 */
@RestController
@RequestMapping("/api/culture/behavior/review")
public class ReviewController {

    @Autowired private ReviewService service;

    /** 配置列表（文化岗）。 */
    @GetMapping("/config/list")
    public ResponseResult<?> configList() {
        return ResponseResult.success(service.configList(null));
    }

    /** 保存配置（草稿可改，已发布不可改）。 */
    @PostMapping("/config")
    public ResponseResult<?> saveConfig(@RequestBody BehaviorReviewConfig body) {
        return ResponseResult.success(service.saveConfig(body));
    }

    /** 发布配置。 */
    @PostMapping("/config/{id}/publish")
    public ResponseResult<?> publishConfig(@PathVariable Long id) {
        service.publishConfig(id);
        return ResponseResult.success(null);
    }

    /** 结束配置。 */
    @PostMapping("/config/{id}/finish")
    public ResponseResult<?> finishConfig(@PathVariable Long id) {
        service.finishConfig(id);
        return ResponseResult.success(null);
    }

    /** 我待评议（窗口内已发布 + 未评）。 */
    @GetMapping("/my-todo")
    public ResponseResult<?> myTodo() {
        return ResponseResult.success(service.myTodo());
    }

    /** 我已评议。 */
    @GetMapping("/my-given")
    public ResponseResult<?> myGiven() {
        return ResponseResult.success(service.myGiven());
    }

    /** 我收到的评议（文化岗/C07 可看全部）。 */
    @GetMapping("/my-received")
    public ResponseResult<?> myReceived() {
        return ResponseResult.success(service.myReceived());
    }

    /** 提交评议，body {configId, evaluatee, relation, dimCustomer, dimStrive, dimHardship, dimReflect, comment}。 */
    @PostMapping("/submit")
    public ResponseResult<?> submit(@RequestBody BehaviorReviewRecord body) {
        return ResponseResult.success(service.submitReview(body));
    }
}
