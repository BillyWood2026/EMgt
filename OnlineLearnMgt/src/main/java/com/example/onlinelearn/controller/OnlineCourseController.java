package com.example.onlinelearn.controller;

import com.example.common.dto.ResponseResult;
import com.example.onlinelearn.entity.OnlineCourse;
import com.example.onlinelearn.service.OnlineCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在线课程（learn-to-exam/e-learning/Course.vue，前缀 /api/learn/courses）
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/learn/courses")
public class OnlineCourseController {

    @Autowired
    private OnlineCourseService onlineCourseService;

    /** 课程列表（前端读取 data.records || data.data，返回数组即可） */
    @GetMapping("/list")
    public ResponseResult<List<OnlineCourse>> list(@RequestParam(required = false) Integer pageNum,
                                                    @RequestParam(required = false) Integer pageSize,
                                                    @RequestParam(required = false) String name,
                                                    @RequestParam(required = false) String category,
                                                    @RequestParam(required = false) String required,
                                                    @RequestParam(required = false) String status) {
        return ResponseResult.success(onlineCourseService.list());
    }

    @GetMapping("/{id}")
    public ResponseResult<OnlineCourse> getById(@PathVariable Long id) {
        return ResponseResult.success(onlineCourseService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody OnlineCourse onlineCourse) {
        return ResponseResult.success(onlineCourseService.save(onlineCourse));
    }

    /** 更新（id 在请求体中，路径无 /{id}） */
    @PutMapping
    public ResponseResult<Boolean> update(@RequestBody OnlineCourse onlineCourse) {
        return ResponseResult.success(onlineCourseService.updateById(onlineCourse));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(onlineCourseService.removeById(id));
    }
}
