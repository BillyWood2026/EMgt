package com.example.culturemgt.foundation.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.foundation.entity.Creed;
import com.example.culturemgt.foundation.service.CreedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/culture/foundation/creed")
public class CreedController {

    @Autowired private CreedService service;

    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String status,
                                  @RequestParam(required = false) String applyOrg) {
        return ResponseResult.success(service.list(status, applyOrg));
    }

    @GetMapping("/active")
    public ResponseResult<?> active() {
        return ResponseResult.success(service.active());
    }

    @GetMapping("/{id}")
    public ResponseResult<?> get(@PathVariable Long id) {
        return ResponseResult.success(service.get(id));
    }

    @PostMapping
    public ResponseResult<?> create(@RequestBody Creed c) {
        c.setId(null);
        return ResponseResult.success(service.save(c));
    }

    @PutMapping("/{id}")
    public ResponseResult<?> update(@PathVariable Long id, @RequestBody Creed c) {
        c.setId(id);
        return ResponseResult.success(service.save(c));
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
