package com.example.culturemgt.survey.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.survey.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 文化健康度分析仪表盘 / 部门氛围诊断抽屉（ELCUL010）。 */
@RestController
@RequestMapping("/api/culture/survey-health")
public class SurveyHealthController {

    @Autowired private HealthService service;

    /** 仪表盘总览：最新周期各部门氛围指数+集团整体+趋势。 */
    @GetMapping("/overview")
    public ResponseResult<?> overview(@RequestParam(required = false) Long questionnaireId) {
        return ResponseResult.success(service.overview(questionnaireId));
    }

    /** 部门诊断抽屉：维度得分环比/短板/词云/题项分布/趋势/整改台账。 */
    @GetMapping("/org")
    public ResponseResult<?> org(@RequestParam String dept,
                                 @RequestParam(required = false) Long questionnaireId) {
        return ResponseResult.success(service.orgDetail(dept, questionnaireId));
    }

    /** 多周期趋势：dept 不传=集团整体。 */
    @GetMapping("/trend")
    public ResponseResult<?> trend(@RequestParam(required = false) String dept) {
        return ResponseResult.success(service.trend(dept));
    }
}
