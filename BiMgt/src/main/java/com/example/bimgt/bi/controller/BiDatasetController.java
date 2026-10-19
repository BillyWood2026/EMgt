package com.example.bimgt.bi.controller;

import com.example.bimgt.bi.entity.BiDataset;
import com.example.bimgt.bi.service.BiDatasetService;
import com.example.bimgt.common.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bi/dataset")
public class BiDatasetController {

    private final BiDatasetService service;

    public BiDatasetController(BiDatasetService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public ResponseResult<List<BiDataset>> list() {
        return ResponseResult.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public ResponseResult<BiDataset> get(@PathVariable Long id) {
        return ResponseResult.ok(service.getById(id));
    }

    @PostMapping
    public ResponseResult<BiDataset> save(@RequestBody BiDataset ds) {
        return ResponseResult.ok(service.upsert(ds));
    }

    @PutMapping("/{id}")
    public ResponseResult<BiDataset> update(@PathVariable Long id, @RequestBody BiDataset ds) {
        ds.setId(id);
        return ResponseResult.ok(service.upsert(ds));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(service.removeById(id));
    }
}
