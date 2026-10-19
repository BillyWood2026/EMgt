package com.example.culturemgt.honor.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.honor.entity.HonorProject;
import com.example.culturemgt.honor.service.HonorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 荣誉项目配置（写操作仅 C03/C07/C99）。 */
@RestController
@RequestMapping("/api/culture/honor-project")
public class HonorProjectController {

    @Autowired private HonorService service;

    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String status) {
        return ResponseResult.success(service.projects(status));
    }

    @PostMapping
    public ResponseResult<?> create(@RequestBody HonorProject p) {
        p.setId(null);
        return ResponseResult.success(service.saveProject(p));
    }

    @PutMapping("/{id}")
    public ResponseResult<?> update(@PathVariable Long id, @RequestBody HonorProject p) {
        p.setId(id);
        return ResponseResult.success(service.saveProject(p));
    }

    @PostMapping("/{id}/toggle")
    public ResponseResult<?> toggle(@PathVariable Long id) {
        service.toggleProject(id);
        return ResponseResult.success("已切换状态");
    }
}
