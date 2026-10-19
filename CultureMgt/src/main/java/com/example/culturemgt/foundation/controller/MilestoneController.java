package com.example.culturemgt.foundation.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.foundation.entity.Milestone;
import com.example.culturemgt.foundation.service.MilestoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/culture/foundation/milestone")
public class MilestoneController {

    @Autowired private MilestoneService service;

    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String status,
                                  @RequestParam(required = false) String front) {
        return ResponseResult.success(service.list(status, front));
    }

    @PostMapping
    public ResponseResult<?> create(@RequestBody Milestone m) {
        m.setId(null);
        return ResponseResult.success(service.save(m));
    }

    @PutMapping("/{id}")
    public ResponseResult<?> update(@PathVariable Long id, @RequestBody Milestone m) {
        m.setId(id);
        return ResponseResult.success(service.save(m));
    }

    @PostMapping("/{id}/toggle")
    public ResponseResult<?> toggle(@PathVariable Long id, @RequestParam String action) {
        service.toggle(id, action);
        return ResponseResult.success(true);
    }
}
