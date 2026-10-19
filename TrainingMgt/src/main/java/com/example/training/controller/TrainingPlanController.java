package com.example.training.controller;

import com.example.common.dto.ResponseResult;
import com.example.training.entity.TrainingPlan;
import com.example.training.service.TrainingPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 培训计划（training-mgt/Plan.vue）
 * 根列表：GET /api/training/plan（无 /list 后缀）；/list 为健康检查别名
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/training/plan")
public class TrainingPlanController {

    @Autowired
    private TrainingPlanService trainingPlanService;

    /** 计划列表（前端实际调用，无 /list 后缀） */
    @GetMapping
    public ResponseResult<List<TrainingPlan>> list(@RequestParam(required = false) String trainingType,
                                                   @RequestParam(required = false) String planName) {
        return ResponseResult.success(trainingPlanService.listPlans(trainingType, planName));
    }

    /** 别名端点，供健康检查使用，返回同样的数组 */
    @GetMapping("/list")
    public ResponseResult<List<TrainingPlan>> listAlias(@RequestParam(required = false) String trainingType,
                                                        @RequestParam(required = false) String planName) {
        return ResponseResult.success(trainingPlanService.listPlans(trainingType, planName));
    }

    @GetMapping("/{id}")
    public ResponseResult<TrainingPlan> getById(@PathVariable Long id) {
        return ResponseResult.success(trainingPlanService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody TrainingPlan trainingPlan) {
        return ResponseResult.success(trainingPlanService.save(trainingPlan));
    }

    /** 更新（id 在请求体中，路径无 /{id}） */
    @PutMapping
    public ResponseResult<Boolean> update(@RequestBody TrainingPlan trainingPlan) {
        return ResponseResult.success(trainingPlanService.updateById(trainingPlan));
    }

    /** 提交/开始执行：状态置为已提交 */
    @PostMapping("/{id}/submit")
    public ResponseResult<Boolean> submit(@PathVariable Long id) {
        TrainingPlan plan = trainingPlanService.getById(id);
        if (plan == null) {
            return ResponseResult.success(false);
        }
        plan.setStatus("已提交");
        return ResponseResult.success(trainingPlanService.updateById(plan));
    }
}
