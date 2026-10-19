package com.example.talent.controller;

import com.example.common.dto.ResponseResult;
import com.example.talent.entity.EvaluationModel;
import com.example.talent.service.EvaluationModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Evaluation Model Controller (评估模型)
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/talent/model")
public class EvaluationModelController {

    @Autowired
    private EvaluationModelService evaluationModelService;

    @GetMapping("/list")
    public ResponseResult<List<EvaluationModel>> list() {
        return ResponseResult.success(evaluationModelService.list());
    }

    @GetMapping("/{id}")
    public ResponseResult<EvaluationModel> getById(@PathVariable Long id) {
        return ResponseResult.success(evaluationModelService.getById(id));
    }

    @PostMapping
    public ResponseResult<Void> create(@RequestBody EvaluationModel evaluationModel) {
        evaluationModelService.save(evaluationModel);
        return ResponseResult.success();
    }

    @PutMapping("/{id}")
    public ResponseResult<Void> update(@PathVariable Long id, @RequestBody EvaluationModel evaluationModel) {
        evaluationModel.setId(id);
        evaluationModelService.updateById(evaluationModel);
        return ResponseResult.success();
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        evaluationModelService.removeById(id);
        return ResponseResult.success();
    }
}
