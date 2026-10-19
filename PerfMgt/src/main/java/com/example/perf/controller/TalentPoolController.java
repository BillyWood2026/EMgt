package com.example.perf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.dto.ResponseResult;
import com.example.perf.entity.TalentPool;
import com.example.perf.mapper.TalentPoolMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 人才池 Controller（base: /api/talent-pool）
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/talent-pool")
public class TalentPoolController {

    @Autowired
    private TalentPoolMapper talentPoolMapper;

    @GetMapping("/list")
    public ResponseResult<List<TalentPool>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String poolType) {
        LambdaQueryWrapper<TalentPool> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            qw.and(w -> w.like(TalentPool::getEmployeeName, kw)
                    .or().like(TalentPool::getEmployeeNo, kw)
                    .or().like(TalentPool::getPositionName, kw));
        }
        if (StringUtils.hasText(status)) {
            qw.eq(TalentPool::getStatus, status.trim());
        }
        if (StringUtils.hasText(poolType)) {
            qw.eq(TalentPool::getPoolType, poolType.trim());
        }
        qw.orderByDesc(TalentPool::getId);
        return ResponseResult.success(talentPoolMapper.selectList(qw));
    }

    @GetMapping("/{id}")
    public ResponseResult<TalentPool> getById(@PathVariable Long id) {
        return ResponseResult.success(talentPoolMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<TalentPool> create(@RequestBody TalentPool body) {
        LocalDateTime now = LocalDateTime.now();
        body.setId(null);
        body.setDeleted(0);
        body.setVersion(1);
        body.setCreateTime(now);
        body.setUpdateTime(now);
        if (!StringUtils.hasText(body.getStatus())) {
            body.setStatus("ACTIVE");
        }
        talentPoolMapper.insert(body);
        return ResponseResult.success(body);
    }

    @PutMapping("/{id}")
    public ResponseResult<TalentPool> update(@PathVariable Long id, @RequestBody TalentPool body) {
        body.setId(id);
        body.setUpdateTime(LocalDateTime.now());
        talentPoolMapper.updateById(body);
        return ResponseResult.success(talentPoolMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        talentPoolMapper.deleteById(id);
        return ResponseResult.success();
    }
}
