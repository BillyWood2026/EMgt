package com.example.screenmgt.screen.controller;

import com.example.screenmgt.common.ResponseResult;
import com.example.screenmgt.screen.entity.ScreenDatasource;
import com.example.screenmgt.screen.service.ScreenDatasourceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screen/datasource")
public class ScreenDatasourceController {

    private final ScreenDatasourceService service;

    public ScreenDatasourceController(ScreenDatasourceService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public ResponseResult<List<ScreenDatasource>> list() {
        return ResponseResult.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public ResponseResult<ScreenDatasource> get(@PathVariable Long id) {
        return ResponseResult.ok(service.getById(id));
    }

    @PostMapping
    public ResponseResult<ScreenDatasource> save(@RequestBody ScreenDatasource d) {
        return ResponseResult.ok(service.upsert(d));
    }

    @PutMapping("/{id}")
    public ResponseResult<ScreenDatasource> update(@PathVariable Long id, @RequestBody ScreenDatasource d) {
        d.setId(id);
        return ResponseResult.ok(service.upsert(d));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(service.removeById(id));
    }
}
