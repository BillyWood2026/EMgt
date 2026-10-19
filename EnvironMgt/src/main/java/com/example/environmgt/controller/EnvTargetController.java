package com.example.environmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.environmgt.common.ResponseResult;
import com.example.environmgt.entity.EnvTarget;
import com.example.environmgt.service.EnvTargetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 环境目标管理
 */
@RestController
@RequestMapping("/api/eehs/env/targets")
public class EnvTargetController {

    @Autowired
    private EnvTargetService service;

    @GetMapping
    public ResponseResult<List<EnvTarget>> list(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String factory,
                                                @RequestParam(required = false) String status) {
        QueryWrapper<EnvTarget> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("target_name", keyword).or().like("target_no", keyword).or().like("owner", keyword));
        }
        if (StringUtils.hasText(factory)) wrapper.eq("factory", factory);
        if (StringUtils.hasText(status)) wrapper.eq("status", status);
        wrapper.orderByDesc("id");
        return ResponseResult.success(service.list(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<EnvTarget> getById(@PathVariable Long id) {
        return ResponseResult.success(service.getById(id));
    }

    @PostMapping
    public ResponseResult<EnvTarget> save(@RequestBody EnvTarget entity) {
        if (entity.getId() == null) {
            entity.setId(null);
            entity.setTargetNo(nextNo());
            if (!StringUtils.hasText(entity.getStatus())) entity.setStatus("草稿");
            entity.setCreateTime(LocalDateTime.now());
        }
        service.saveOrUpdate(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<EnvTarget> update(@PathVariable Long id, @RequestBody EnvTarget entity) {
        entity.setId(id);
        service.updateById(entity);
        return ResponseResult.success(entity);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return ResponseResult.success();
    }

    private String nextNo() {
        String prefix = "ET" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long cnt = service.count(new QueryWrapper<EnvTarget>().likeRight("target_no", prefix));
        return prefix + String.format("%04d", cnt + 1);
    }
}
