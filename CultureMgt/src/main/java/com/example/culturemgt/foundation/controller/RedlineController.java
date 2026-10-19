package com.example.culturemgt.foundation.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.foundation.entity.Redline;
import com.example.culturemgt.foundation.service.RedlineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/culture/foundation/redline")
public class RedlineController {

    @Autowired private RedlineService service;

    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String status,
                                  @RequestParam(required = false) String level,
                                  @RequestParam(required = false) String category) {
        return ResponseResult.success(service.list(status, level, category));
    }

    @GetMapping("/{id}")
    public ResponseResult<?> get(@PathVariable Long id) {
        return ResponseResult.success(service.get(id));
    }

    @PostMapping
    public ResponseResult<?> create(@RequestBody Redline r) {
        r.setId(null);
        return ResponseResult.success(service.save(r));
    }

    @PutMapping("/{id}")
    public ResponseResult<?> update(@PathVariable Long id, @RequestBody Redline r) {
        r.setId(id);
        return ResponseResult.success(service.save(r));
    }

    @PostMapping("/{id}/submit")
    public ResponseResult<?> submit(@PathVariable Long id) {
        service.submit(id);
        return ResponseResult.success(true);
    }

    @PostMapping("/{id}/approve")
    public ResponseResult<?> approve(@PathVariable Long id,
                                     @RequestParam boolean pass,
                                     @RequestParam(required = false) String opinion) {
        service.approve(id, pass, opinion);
        return ResponseResult.success(true);
    }

    @PostMapping("/{id}/archive")
    public ResponseResult<?> archive(@PathVariable Long id, @RequestParam String reason) {
        service.archive(id, reason);
        return ResponseResult.success(true);
    }
}
