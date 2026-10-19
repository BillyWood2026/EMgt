package com.example.culturemgt.training.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.training.entity.Course;
import com.example.culturemgt.training.service.CourseService;
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

/** 文化课程元数据维护（后台）。 */
@RestController
@RequestMapping("/api/culture/course")
public class CourseController {

    @Autowired private CourseService service;

    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String category,
                                  @RequestParam(required = false) String status) {
        return ResponseResult.success(service.list(category, status));
    }

    @PostMapping
    public ResponseResult<?> create(@RequestBody Course c) {
        c.setId(null);
        return ResponseResult.success(service.save(c));
    }

    @PutMapping("/{id}")
    public ResponseResult<?> update(@PathVariable Long id, @RequestBody Course c) {
        c.setId(id);
        return ResponseResult.success(service.save(c));
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
}
