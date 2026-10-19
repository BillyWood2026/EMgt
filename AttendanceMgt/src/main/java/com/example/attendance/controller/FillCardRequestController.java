package com.example.attendance.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.attendance.entity.FillCardRequest;
import com.example.attendance.service.FillCardRequestService;
import com.example.common.dto.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 补卡申请 标准 CRUD
 * （原提交入口 POST /api/attendance/clock-in/fill-card-request 保持不变）
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/attendance/fill-card-request")
public class FillCardRequestController {

    @Autowired
    private FillCardRequestService fillCardRequestService;

    @GetMapping("/list")
    public ResponseResult<List<FillCardRequest>> list() {
        return ResponseResult.success(
                fillCardRequestService.list(
                        Wrappers.<FillCardRequest>lambdaQuery().orderByDesc(FillCardRequest::getId)));
    }

    @GetMapping("/{id}")
    public ResponseResult<FillCardRequest> getById(@PathVariable Long id) {
        return ResponseResult.success(fillCardRequestService.getById(id));
    }

    @PostMapping
    public ResponseResult<FillCardRequest> create(@RequestBody FillCardRequest request) {
        fillCardRequestService.save(request);
        return ResponseResult.success(request);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody FillCardRequest request) {
        request.setId(id);
        return ResponseResult.success(fillCardRequestService.updateById(request));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(fillCardRequestService.removeById(id));
    }
}
