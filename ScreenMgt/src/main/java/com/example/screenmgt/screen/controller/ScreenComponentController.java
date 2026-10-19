package com.example.screenmgt.screen.controller;

import com.example.screenmgt.common.ResponseResult;
import com.example.screenmgt.screen.entity.ScreenComponent;
import com.example.screenmgt.screen.service.ScreenComponentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screen/component")
public class ScreenComponentController {

    private final ScreenComponentService service;

    public ScreenComponentController(ScreenComponentService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public ResponseResult<List<ScreenComponent>> list() {
        return ResponseResult.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public ResponseResult<ScreenComponent> get(@PathVariable Long id) {
        return ResponseResult.ok(service.getById(id));
    }

    @PostMapping
    public ResponseResult<ScreenComponent> save(@RequestBody ScreenComponent c) {
        return ResponseResult.ok(service.upsert(c));
    }

    @PutMapping("/{id}")
    public ResponseResult<ScreenComponent> update(@PathVariable Long id, @RequestBody ScreenComponent c) {
        c.setId(id);
        return ResponseResult.ok(service.upsert(c));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(service.removeById(id));
    }
}
