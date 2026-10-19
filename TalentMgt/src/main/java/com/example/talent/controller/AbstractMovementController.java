package com.example.talent.controller;

import com.example.common.dto.ResponseResult;
import com.example.talent.entity.MovementOrder;
import com.example.talent.service.MovementOrderService;
import com.example.talent.service.impl.MovementOrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 异动/调动通用 Controller 逻辑。
 * 具体前缀由子类的类级 @RequestMapping 决定：
 * /api/movement（异动）与 /api/transfer（调动别名）。
 */
public abstract class AbstractMovementController {

    @Autowired
    protected MovementOrderService movementOrderService;

    @GetMapping("/list")
    public ResponseResult<List<MovementOrder>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String movementType) {
        return ResponseResult.success(movementOrderService.search(keyword, status, movementType));
    }

    @GetMapping("/{id}")
    public ResponseResult<MovementOrder> getById(@PathVariable Long id) {
        return ResponseResult.success(movementOrderService.getById(id));
    }

    @PostMapping
    public ResponseResult<MovementOrder> create(@RequestBody MovementOrder body) {
        LocalDateTime now = LocalDateTime.now();
        body.setId(null);
        body.setDeleted(0);
        body.setVersion(1);
        body.setCreateTime(now);
        body.setUpdateTime(now);
        if (!StringUtils.hasText(body.getStatus())) {
            body.setStatus("DRAFT");
        }
        MovementOrderServiceImpl.fillAdjustment(body);
        movementOrderService.save(body);
        return ResponseResult.success(body);
    }

    @PutMapping("/{id}")
    public ResponseResult<MovementOrder> update(@PathVariable Long id,
                                                @RequestBody MovementOrder body) {
        body.setId(id);
        body.setUpdateTime(LocalDateTime.now());
        MovementOrderServiceImpl.fillAdjustment(body);
        movementOrderService.updateById(body);
        return ResponseResult.success(movementOrderService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        movementOrderService.removeById(id);
        return ResponseResult.success();
    }

    @PostMapping("/{id}/submit")
    public ResponseResult<MovementOrder> submit(@PathVariable Long id) {
        return ResponseResult.success(movementOrderService.submit(id));
    }

    @PostMapping("/{id}/approve")
    public ResponseResult<MovementOrder> approve(@PathVariable Long id) {
        return ResponseResult.success(movementOrderService.approve(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseResult<MovementOrder> reject(@PathVariable Long id) {
        return ResponseResult.success(movementOrderService.reject(id));
    }
}
