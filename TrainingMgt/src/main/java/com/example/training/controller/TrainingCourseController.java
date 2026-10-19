package com.example.training.controller;

import com.example.common.dto.ResponseResult;
import com.example.training.entity.TrainingCourse;
import com.example.training.service.TrainingCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 离线培训课程（training-mgt/Course.vue）
 * 根列表：GET /api/training/course（无 /list 后缀）；/list 为健康检查别名
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/training/course")
public class TrainingCourseController {

    @Autowired
    private TrainingCourseService trainingCourseService;

    /** 课程列表（前端实际调用，无 /list 后缀） */
    @GetMapping
    public ResponseResult<List<TrainingCourse>> list(@RequestParam(required = false) String courseType,
                                                     @RequestParam(required = false) String name) {
        return ResponseResult.success(trainingCourseService.listCourses(courseType, name));
    }

    /** 别名端点，供健康检查使用，返回同样的数组 */
    @GetMapping("/list")
    public ResponseResult<List<TrainingCourse>> listAlias(@RequestParam(required = false) String courseType,
                                                          @RequestParam(required = false) String name) {
        return ResponseResult.success(trainingCourseService.listCourses(courseType, name));
    }

    @GetMapping("/{id}")
    public ResponseResult<TrainingCourse> getById(@PathVariable Long id) {
        return ResponseResult.success(trainingCourseService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody TrainingCourse trainingCourse) {
        return ResponseResult.success(trainingCourseService.save(trainingCourse));
    }

    /** 更新（id 在请求体中，路径无 /{id}） */
    @PutMapping
    public ResponseResult<Boolean> update(@RequestBody TrainingCourse trainingCourse) {
        return ResponseResult.success(trainingCourseService.updateById(trainingCourse));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(trainingCourseService.removeById(id));
    }

    /** 发布：状态置为已发布 */
    @PostMapping("/{id}/publish")
    public ResponseResult<Boolean> publish(@PathVariable Long id) {
        TrainingCourse course = trainingCourseService.getById(id);
        if (course == null) {
            return ResponseResult.success(false);
        }
        course.setStatus("已发布");
        return ResponseResult.success(trainingCourseService.updateById(course));
    }

    /** 下线：状态置为已下架 */
    @PostMapping("/{id}/offline")
    public ResponseResult<Boolean> offline(@PathVariable Long id) {
        TrainingCourse course = trainingCourseService.getById(id);
        if (course == null) {
            return ResponseResult.success(false);
        }
        course.setStatus("已下架");
        return ResponseResult.success(trainingCourseService.updateById(course));
    }
}
