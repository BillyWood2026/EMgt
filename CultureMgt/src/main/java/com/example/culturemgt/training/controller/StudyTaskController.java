package com.example.culturemgt.training.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.training.entity.StudyRecord;
import com.example.culturemgt.training.entity.StudyTask;
import com.example.culturemgt.training.service.StudyTaskService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 学习任务配置（后台）+ 我的学习（前台）。 */
@RestController
@RequestMapping("/api/culture/study-task")
public class StudyTaskController {

    @Autowired private StudyTaskService service;

    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String taskType,
                                  @RequestParam(required = false) String status) {
        return ResponseResult.success(service.list(taskType, status));
    }

    @PostMapping
    public ResponseResult<?> create(@RequestBody StudyTask t) {
        t.setId(null);
        return ResponseResult.success(service.save(t));
    }

    @PutMapping("/{id}")
    public ResponseResult<?> update(@PathVariable Long id, @RequestBody StudyTask t) {
        t.setId(id);
        return ResponseResult.success(service.save(t));
    }

    @PostMapping("/{id}/toggle")
    public ResponseResult<?> toggle(@PathVariable Long id) {
        service.toggle(id);
        return ResponseResult.success("已切换状态");
    }

    /** 分配学习对象，body {users:[{userName,userDept}]}。 */
    @PostMapping("/{id}/assign")
    public ResponseResult<?> assign(@PathVariable Long id, @RequestBody Map<String, List<Map<String, String>>> body) {
        return ResponseResult.success(service.assign(id, body.get("users")));
    }

    /** 任务学习明细与完成率。 */
    @GetMapping("/{id}/records")
    public ResponseResult<?> records(@PathVariable Long id) {
        return ResponseResult.success(service.records(id));
    }

    /** 到期提醒名单：临近截止/已逾期未完成。 */
    @GetMapping("/{id}/remind")
    public ResponseResult<?> remind(@PathVariable Long id) {
        return ResponseResult.success(service.remind(id));
    }

    /** 模拟培训中台回写学习进度，body {userName, progress}。 */
    @PostMapping("/{id}/sync")
    public ResponseResult<?> sync(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String userName = String.valueOf(body.getOrDefault("userName", ""));
        Integer progress = body.get("progress") == null ? null : Integer.valueOf(String.valueOf(body.get("progress")));
        return ResponseResult.success(service.sync(id, userName, progress));
    }

    /** 我的学习任务与进度（前台学习中心）。 */
    @GetMapping("/my-records")
    public ResponseResult<?> myRecords() {
        return ResponseResult.success(service.myRecords());
    }
}
