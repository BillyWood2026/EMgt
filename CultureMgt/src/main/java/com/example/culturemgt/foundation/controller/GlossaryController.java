package com.example.culturemgt.foundation.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.foundation.entity.Glossary;
import com.example.culturemgt.foundation.service.GlossaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/culture/foundation/glossary")
public class GlossaryController {

    @Autowired private GlossaryService service;

    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String status,
                                  @RequestParam(required = false) String valueTag) {
        return ResponseResult.success(service.list(status, valueTag));
    }

    @PostMapping
    public ResponseResult<?> create(@RequestBody Glossary g) {
        g.setId(null);
        return ResponseResult.success(service.save(g));
    }

    @PutMapping("/{id}")
    public ResponseResult<?> update(@PathVariable Long id, @RequestBody Glossary g) {
        g.setId(id);
        return ResponseResult.success(service.save(g));
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
    public ResponseResult<?> archive(@PathVariable Long id) {
        service.archive(id);
        return ResponseResult.success(true);
    }
}
