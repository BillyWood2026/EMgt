package com.example.perf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.dto.ResponseResult;
import com.example.perf.entity.SuccessionRecord;
import com.example.perf.mapper.SuccessionRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 继任候选人 Controller（base: /api/succession）
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/succession")
public class SuccessionRecordController {

    @Autowired
    private SuccessionRecordMapper successionRecordMapper;

    @GetMapping("/list")
    public ResponseResult<List<SuccessionRecord>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String readiness) {
        LambdaQueryWrapper<SuccessionRecord> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            qw.and(w -> w.like(SuccessionRecord::getEmployeeName, kw)
                    .or().like(SuccessionRecord::getEmployeeNo, kw)
                    .or().like(SuccessionRecord::getPositionName, kw));
        }
        if (StringUtils.hasText(status)) {
            qw.eq(SuccessionRecord::getStatus, status.trim());
        }
        if (StringUtils.hasText(readiness)) {
            qw.eq(SuccessionRecord::getReadiness, readiness.trim());
        }
        qw.orderByDesc(SuccessionRecord::getId);
        return ResponseResult.success(successionRecordMapper.selectList(qw));
    }

    @GetMapping("/{id}")
    public ResponseResult<SuccessionRecord> getById(@PathVariable Long id) {
        return ResponseResult.success(successionRecordMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<SuccessionRecord> create(@RequestBody SuccessionRecord body) {
        LocalDateTime now = LocalDateTime.now();
        body.setId(null);
        body.setDeleted(0);
        body.setVersion(1);
        body.setCreateTime(now);
        body.setUpdateTime(now);
        if (!StringUtils.hasText(body.getStatus())) {
            body.setStatus("草稿");
        }
        successionRecordMapper.insert(body);
        return ResponseResult.success(body);
    }

    @PutMapping("/{id}")
    public ResponseResult<SuccessionRecord> update(@PathVariable Long id, @RequestBody SuccessionRecord body) {
        body.setId(id);
        body.setUpdateTime(LocalDateTime.now());
        successionRecordMapper.updateById(body);
        return ResponseResult.success(successionRecordMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        successionRecordMapper.deleteById(id);
        return ResponseResult.success();
    }
}
