package com.example.culturemgt.activity.controller;

import com.example.culturemgt.activity.entity.CultureMaterial;
import com.example.culturemgt.activity.service.MaterialService;
import com.example.culturemgt.common.ResponseResult;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/culture/material")
public class MaterialController {

    @Autowired private MaterialService service;

    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String status,
                                  @RequestParam(required = false) String type) {
        return ResponseResult.success(service.list(status, type));
    }

    /** 出入库台账（可按物料过滤）。 */
    @GetMapping("/flows")
    public ResponseResult<?> flows(@RequestParam(required = false) Long materialId) {
        return ResponseResult.success(service.flows(materialId));
    }

    @PostMapping
    public ResponseResult<?> create(@RequestBody CultureMaterial m) {
        m.setId(null);
        return ResponseResult.success(service.save(m));
    }

    @PutMapping("/{id}")
    public ResponseResult<?> update(@PathVariable Long id, @RequestBody CultureMaterial m) {
        m.setId(id);
        return ResponseResult.success(service.save(m));
    }

    /** 出入库：body {flowType, qty, activityId?, reason?}。 */
    @PostMapping("/{id}/flow")
    public ResponseResult<?> flow(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String flowType = (String) body.get("flowType");
        int qty = Integer.parseInt(String.valueOf(body.get("qty")));
        Long activityId = body.get("activityId") == null || String.valueOf(body.get("activityId")).isBlank()
                ? null : Long.parseLong(String.valueOf(body.get("activityId")));
        String reason = (String) body.get("reason");
        service.flow(id, flowType, qty, activityId, reason);
        return ResponseResult.success(true);
    }

    @PostMapping("/{id}/toggle")
    public ResponseResult<?> toggle(@PathVariable Long id) {
        service.toggle(id);
        return ResponseResult.success(true);
    }
}
