package com.example.bimgt.bi.controller;

import com.example.bimgt.bi.entity.BiDatasource;
import com.example.bimgt.bi.service.BiDatasourceService;
import com.example.bimgt.common.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bi/datasource")
public class BiDatasourceController {

    private final BiDatasourceService service;

    public BiDatasourceController(BiDatasourceService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public ResponseResult<List<BiDatasource>> list() {
        return ResponseResult.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public ResponseResult<BiDatasource> get(@PathVariable Long id) {
        return ResponseResult.ok(service.getById(id));
    }

    @PostMapping
    public ResponseResult<BiDatasource> save(@RequestBody BiDatasource ds) {
        return ResponseResult.ok(service.upsert(ds));
    }

    @PutMapping("/{id}")
    public ResponseResult<BiDatasource> update(@PathVariable Long id, @RequestBody BiDatasource ds) {
        ds.setId(id);
        return ResponseResult.ok(service.upsert(ds));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(service.removeById(id));
    }
}
