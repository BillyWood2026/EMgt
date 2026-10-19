package com.example.onlineexam.controller;

import com.example.common.dto.ResponseResult;
import com.example.onlineexam.entity.OnlineExam;
import com.example.onlineexam.service.OnlineExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在线考试（learn-to-exam/e-examing/Exam.vue，前缀 /api/learn/exams）
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/learn/exams")
public class OnlineExamController {

    @Autowired
    private OnlineExamService onlineExamService;

    /** 考试列表（前端读取 data.records || data.data，返回数组即可） */
    @GetMapping("/list")
    public ResponseResult<List<OnlineExam>> list(@RequestParam(required = false) Integer pageNum,
                                                  @RequestParam(required = false) Integer pageSize,
                                                  @RequestParam(required = false) String name,
                                                  @RequestParam(required = false) String type,
                                                  @RequestParam(required = false) String status) {
        return ResponseResult.success(onlineExamService.list());
    }

    @GetMapping("/{id}")
    public ResponseResult<OnlineExam> getById(@PathVariable Long id) {
        return ResponseResult.success(onlineExamService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody OnlineExam onlineExam) {
        return ResponseResult.success(onlineExamService.save(onlineExam));
    }

    /** 更新（id 在请求体中，路径无 /{id}） */
    @PutMapping
    public ResponseResult<Boolean> update(@RequestBody OnlineExam onlineExam) {
        return ResponseResult.success(onlineExamService.updateById(onlineExam));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(onlineExamService.removeById(id));
    }
}
