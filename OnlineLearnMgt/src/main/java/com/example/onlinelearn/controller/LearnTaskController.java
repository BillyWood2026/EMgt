package com.example.onlinelearn.controller;

import com.example.common.dto.ResponseResult;
import com.example.onlinelearn.entity.LearnTask;
import com.example.onlinelearn.service.LearnTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学习任务/学习进度（learn-to-exam/e-learning/Progress.vue，前缀 /api/learn/tasks）
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/learn/tasks")
public class LearnTaskController {

    @Autowired
    private LearnTaskService learnTaskService;

    /** 学习进度列表（前端读取 data.records || data.data，返回数组即可） */
    @GetMapping("/list")
    public ResponseResult<List<LearnTask>> list(@RequestParam(required = false) Integer pageNum,
                                                  @RequestParam(required = false) Integer pageSize,
                                                  @RequestParam(required = false) String employee,
                                                  @RequestParam(required = false) String department,
                                                  @RequestParam(required = false) String status) {
        return ResponseResult.success(learnTaskService.list());
    }

    @GetMapping("/{id}")
    public ResponseResult<LearnTask> getById(@PathVariable Long id) {
        return ResponseResult.success(learnTaskService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody LearnTask learnTask) {
        return ResponseResult.success(learnTaskService.save(learnTask));
    }

    /** 更新（id 在请求体中，路径无 /{id}） */
    @PutMapping
    public ResponseResult<Boolean> update(@RequestBody LearnTask learnTask) {
        return ResponseResult.success(learnTaskService.updateById(learnTask));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(learnTaskService.removeById(id));
    }
}
