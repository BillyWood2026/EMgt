package com.example.culturemgt.activity.controller;

import com.example.culturemgt.activity.entity.CulturePosition;
import com.example.culturemgt.activity.service.PositionService;
import com.example.culturemgt.common.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/culture/position")
public class PositionController {

    @Autowired private PositionService service;

    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String status,
                                  @RequestParam(required = false) String type) {
        return ResponseResult.success(service.list(status, type));
    }

    @PostMapping
    public ResponseResult<?> create(@RequestBody CulturePosition p) {
        p.setId(null);
        return ResponseResult.success(service.save(p));
    }

    @PutMapping("/{id}")
    public ResponseResult<?> update(@PathVariable Long id, @RequestBody CulturePosition p) {
        p.setId(id);
        return ResponseResult.success(service.save(p));
    }

    /** 登记一次内容更新（刷新最近更新日期）。 */
    @PostMapping("/{id}/touch")
    public ResponseResult<?> touch(@PathVariable Long id) {
        service.touch(id);
        return ResponseResult.success(true);
    }

    @PostMapping("/{id}/toggle")
    public ResponseResult<?> toggle(@PathVariable Long id) {
        service.toggle(id);
        return ResponseResult.success(true);
    }
}
