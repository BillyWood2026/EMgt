package com.example.screenmgt.screen.controller;

import com.example.screenmgt.common.ResponseResult;
import com.example.screenmgt.screen.entity.ScreenTemplate;
import com.example.screenmgt.screen.service.ScreenTemplateService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screen/template")
public class ScreenTemplateController {

    private final ScreenTemplateService service;

    public ScreenTemplateController(ScreenTemplateService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public ResponseResult<List<ScreenTemplate>> list() {
        return ResponseResult.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public ResponseResult<ScreenTemplate> get(@PathVariable Long id) {
        return ResponseResult.ok(service.getById(id));
    }

    @PostMapping
    public ResponseResult<ScreenTemplate> save(@RequestBody ScreenTemplate t) {
        return ResponseResult.ok(service.upsert(t));
    }

    @PutMapping("/{id}")
    public ResponseResult<ScreenTemplate> update(@PathVariable Long id, @RequestBody ScreenTemplate t) {
        t.setId(id);
        return ResponseResult.ok(service.upsert(t));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(service.removeById(id));
    }
}
