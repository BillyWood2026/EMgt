package com.example.bimgt.bi.controller;

import com.example.bimgt.bi.entity.BiSubscription;
import com.example.bimgt.bi.service.BiSubscriptionService;
import com.example.bimgt.common.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bi/subscription")
public class BiSubscriptionController {

    private final BiSubscriptionService service;

    public BiSubscriptionController(BiSubscriptionService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public ResponseResult<List<BiSubscription>> list() {
        return ResponseResult.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public ResponseResult<BiSubscription> get(@PathVariable Long id) {
        return ResponseResult.ok(service.getById(id));
    }

    @PostMapping
    public ResponseResult<BiSubscription> save(@RequestBody BiSubscription s) {
        return ResponseResult.ok(service.upsert(s));
    }

    @PutMapping("/{id}")
    public ResponseResult<BiSubscription> update(@PathVariable Long id, @RequestBody BiSubscription s) {
        s.setId(id);
        return ResponseResult.ok(service.upsert(s));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(service.removeById(id));
    }
}
