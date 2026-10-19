package com.example.culturemgt.survey.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.survey.service.ResponseService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 前台：我的调研（问卷填写）。 */
@RestController
@RequestMapping("/api/culture/survey-mine")
public class SurveyMineController {

    @Autowired private ResponseService service;

    /** 我的调研：已发布问卷 + 作答状态 + 时间窗。 */
    @GetMapping("/list")
    public ResponseResult<?> myList() {
        return ResponseResult.success(service.myList());
    }

    /** 提交答卷，body {answers:{qid:val}, userDept}。 */
    @PostMapping("/{id}/submit")
    public ResponseResult<?> submit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseResult.success(service.submit(id, body));
    }
}
