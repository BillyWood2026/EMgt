package com.example.culturemgt.survey.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.survey.entity.SurveyDimension;
import com.example.culturemgt.survey.service.DimensionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 氛围维度与权重配置（算法61）。 */
@RestController
@RequestMapping("/api/culture/survey-dimension")
public class SurveyDimensionController {

    @Autowired private DimensionService service;

    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String status) {
        return ResponseResult.success(service.list(status));
    }

    @PostMapping
    public ResponseResult<?> create(@RequestBody SurveyDimension d) {
        d.setId(null);
        return ResponseResult.success(service.save(d));
    }

    @PutMapping("/{id}")
    public ResponseResult<?> update(@PathVariable Long id, @RequestBody SurveyDimension d) {
        d.setId(id);
        return ResponseResult.success(service.save(d));
    }

    @PostMapping("/{id}/toggle")
    public ResponseResult<?> toggle(@PathVariable Long id) {
        return ResponseResult.success(service.toggle(id));
    }
}
