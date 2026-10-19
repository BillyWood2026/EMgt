package com.example.hrplanning.controller;

import com.example.common.dto.ResponseResult;
import com.example.hrplanning.entity.HRPlan;
import com.example.hrplanning.service.HRPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HR Plan Controller (人力规划)
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/hr-plan")
public class HRPlanController {

    @Autowired
    private HRPlanService hrPlanService;

    @GetMapping("/list")
    public ResponseResult<List<HRPlan>> list() {
        return ResponseResult.success(hrPlanService.list());
    }

    @GetMapping("/{id}")
    public ResponseResult<HRPlan> getById(@PathVariable Long id) {
        return ResponseResult.success(hrPlanService.getById(id));
    }

    @PostMapping
    public ResponseResult<Void> create(@RequestBody HRPlan hrPlan) {
        hrPlanService.save(hrPlan);
        return ResponseResult.success();
    }

    @PutMapping("/{id}")
    public ResponseResult<Void> update(@PathVariable Long id, @RequestBody HRPlan hrPlan) {
        hrPlan.setId(id);
        hrPlanService.updateById(hrPlan);
        return ResponseResult.success();
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        hrPlanService.removeById(id);
        return ResponseResult.success();
    }
}
