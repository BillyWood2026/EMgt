package com.example.culturemgt.survey.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.survey.entity.SurveyQuestionnaire;
import com.example.culturemgt.survey.service.QuestionnaireService;
import com.example.culturemgt.survey.service.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 问卷编辑器/模板库/发布/回收进度/答卷明细（后台）。 */
@RestController
@RequestMapping("/api/culture/survey-questionnaire")
public class QuestionnaireController {

    @Autowired private QuestionnaireService service;
    @Autowired private ResponseService responseService;

    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String status,
                                  @RequestParam(required = false) String periodType,
                                  @RequestParam(required = false) Boolean templateOnly) {
        return ResponseResult.success(service.list(status, periodType, templateOnly));
    }

    @GetMapping("/{id}")
    public ResponseResult<?> get(@PathVariable Long id) {
        return ResponseResult.success(service.get(id));
    }

    @PostMapping
    public ResponseResult<?> create(@RequestBody SurveyQuestionnaire q) {
        q.setId(null);
        return ResponseResult.success(service.save(q));
    }

    @PutMapping("/{id}")
    public ResponseResult<?> update(@PathVariable Long id, @RequestBody SurveyQuestionnaire q) {
        q.setId(id);
        return ResponseResult.success(service.save(q));
    }

    /** 从内置模板复制创建草稿。 */
    @PostMapping("/template/{id}/copy")
    public ResponseResult<?> copyTemplate(@PathVariable Long id) {
        return ResponseResult.success(service.copyTemplate(id));
    }

    /** 发布问卷，body 可覆盖 targetCount/startTime/endTime/targetOrg。 */
    @PostMapping("/{id}/publish")
    public ResponseResult<?> publish(@PathVariable Long id, @RequestBody(required = false) SurveyQuestionnaire body) {
        return ResponseResult.success(service.publish(id, body == null ? new SurveyQuestionnaire() : body));
    }

    @PostMapping("/{id}/finish")
    public ResponseResult<?> finish(@PathVariable Long id) {
        return ResponseResult.success(service.finish(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseResult.success("已删除");
    }

    /** 回收进度（任务管理页）。 */
    @GetMapping("/{id}/progress")
    public ResponseResult<?> progress(@PathVariable Long id) {
        return ResponseResult.success(service.progress(id));
    }

    /** 答卷明细（后台）。 */
    @GetMapping("/{id}/responses")
    public ResponseResult<?> responses(@PathVariable Long id) {
        return ResponseResult.success(responseService.responses(id));
    }
}
