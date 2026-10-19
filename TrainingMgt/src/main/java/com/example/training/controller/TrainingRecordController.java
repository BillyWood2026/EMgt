package com.example.training.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.example.training.entity.TrainingRecord;
import com.example.training.service.TrainingRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 培训记录（training-mgt/Record.vue）
 * 分页参数 current/size，返回 {records,total,current,size}
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/training/record")
public class TrainingRecordController {

    @Autowired
    private TrainingRecordService trainingRecordService;

    @GetMapping
    public ResponseResult<IPage<TrainingRecord>> list(@RequestParam(defaultValue = "1") long current,
                                                      @RequestParam(defaultValue = "10") long size,
                                                      @RequestParam(required = false) String employeeName,
                                                      @RequestParam(required = false) String department,
                                                      @RequestParam(required = false) String courseName,
                                                      @RequestParam(required = false) String result) {
        return ResponseResult.success(
                trainingRecordService.pageRecords(current, size, employeeName, department, courseName, result));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody TrainingRecord trainingRecord) {
        return ResponseResult.success(trainingRecordService.save(trainingRecord));
    }

    /** 更新（id 在请求体中，路径无 /{id}） */
    @PutMapping
    public ResponseResult<Boolean> update(@RequestBody TrainingRecord trainingRecord) {
        return ResponseResult.success(trainingRecordService.updateById(trainingRecord));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(trainingRecordService.removeById(id));
    }
}
