package com.example.succession.controller;

import com.example.common.dto.ResponseResult;
import com.example.succession.entity.SuccessionPlan;
import com.example.succession.service.SuccessionPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Succession Plan Controller (继任管理)
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/talent/succession")
public class SuccessionPlanController {

    @Autowired
    private SuccessionPlanService successionPlanService;

    @GetMapping({"", "/list"})
    public ResponseResult<List<SuccessionPlan>> list() {
        return ResponseResult.success(successionPlanService.list());
    }

    @GetMapping("/{id}")
    public ResponseResult<SuccessionPlan> getById(@PathVariable Long id) {
        return ResponseResult.success(successionPlanService.getById(id));
    }

    @PostMapping
    public ResponseResult<Void> create(@RequestBody SuccessionPlan successionPlan) {
        successionPlanService.save(successionPlan);
        return ResponseResult.success();
    }

    @PutMapping("/{id}")
    public ResponseResult<Void> update(@PathVariable Long id, @RequestBody SuccessionPlan successionPlan) {
        successionPlan.setId(id);
        successionPlanService.updateById(successionPlan);
        return ResponseResult.success();
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        successionPlanService.removeById(id);
        return ResponseResult.success();
    }
}
