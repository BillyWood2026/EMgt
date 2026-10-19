package com.example.culturemgt.training.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.training.entity.Exam;
import com.example.culturemgt.training.service.ExamService;
import java.util.List;
import java.util.Map;
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

/** 考试任务与监考：配置/分配/开考/切屏上报/交卷阅卷/合格率统计。 */
@RestController
@RequestMapping("/api/culture/exam")
public class ExamController {

    @Autowired private ExamService service;

    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String status) {
        return ResponseResult.success(service.list(status));
    }

    @PostMapping
    public ResponseResult<?> create(@RequestBody Exam e) {
        e.setId(null);
        return ResponseResult.success(service.save(e));
    }

    @PutMapping("/{id}")
    public ResponseResult<?> update(@PathVariable Long id, @RequestBody Exam e) {
        e.setId(id);
        return ResponseResult.success(service.save(e));
    }

    @PostMapping("/{id}/toggle")
    public ResponseResult<?> toggle(@PathVariable Long id) {
        service.toggle(id);
        return ResponseResult.success("已切换状态");
    }

    @DeleteMapping("/{id}")
    public ResponseResult<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseResult.success("已删除");
    }

    /** 分配考试对象，body {users:[{userName,userDept}]}。 */
    @PostMapping("/{id}/assign")
    public ResponseResult<?> assign(@PathVariable Long id, @RequestBody Map<String, List<Map<String, String>>> body) {
        return ResponseResult.success(service.assign(id, body.get("users")));
    }

    /** 监考列表：考试实例（含切屏次数/冻结状态）。 */
    @GetMapping("/{id}/instances")
    public ResponseResult<?> instances(@PathVariable Long id) {
        return ResponseResult.success(service.instances(id));
    }

    /** 开考（首次/补考），返回卷面（不含答案）。 */
    @PostMapping("/instance/{instanceId}/start")
    public ResponseResult<?> start(@PathVariable Long instanceId) {
        return ResponseResult.success(service.start(instanceId));
    }

    /** 防切屏监考上报，body {count}。 */
    @PostMapping("/instance/{instanceId}/switch")
    public ResponseResult<?> switchReport(@PathVariable Long instanceId, @RequestBody Map<String, Object> body) {
        Integer count = body.get("count") == null ? null : Integer.valueOf(String.valueOf(body.get("count")));
        return ResponseResult.success(service.reportSwitch(instanceId, count));
    }

    /** 交卷自动阅卷，body {answers:{qid:ans}, autoSubmit}。 */
    @PostMapping("/instance/{instanceId}/submit")
    public ResponseResult<?> submit(@PathVariable Long instanceId, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        Map<String, String> answers = (Map<String, String>) body.getOrDefault("answers", Map.of());
        Boolean autoSubmit = Boolean.TRUE.equals(body.get("autoSubmit"));
        return ResponseResult.success(service.submit(instanceId, answers, autoSubmit));
    }

    /** 我的考试实例（前台）。 */
    @GetMapping("/my-instances")
    public ResponseResult<?> myInstances() {
        return ResponseResult.success(service.myInstances());
    }

    /** 算法51 培训合格率按部门统计，?examId= 可限定单场。 */
    @GetMapping("/stats")
    public ResponseResult<?> stats(@RequestParam(required = false) Long examId) {
        return ResponseResult.success(service.stats(examId));
    }
}
