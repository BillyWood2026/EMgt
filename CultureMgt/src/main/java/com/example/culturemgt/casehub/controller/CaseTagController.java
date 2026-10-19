package com.example.culturemgt.casehub.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.casehub.entity.CaseTag;
import com.example.culturemgt.casehub.service.CaseTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/culture/case-tag")
public class CaseTagController {

    @Autowired private CaseTagService service;

    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String status) {
        return ResponseResult.success(service.list(status));
    }

    @PostMapping
    public ResponseResult<?> create(@RequestBody CaseTag t) {
        t.setId(null);
        return ResponseResult.success(service.save(t));
    }

    @PutMapping("/{id}")
    public ResponseResult<?> update(@PathVariable Long id, @RequestBody CaseTag t) {
        t.setId(id);
        return ResponseResult.success(service.save(t));
    }

    @PostMapping("/{id}/toggle")
    public ResponseResult<?> toggle(@PathVariable Long id) {
        service.toggle(id);
        return ResponseResult.success(true);
    }
}
