package com.example.hrplanning.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.dto.ResponseResult;
import com.example.hrplanning.entity.TalentLedger;
import com.example.hrplanning.mapper.TalentLedgerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 人才台账 Controller（base: /api/talent-ledger）
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/talent-ledger")
public class TalentLedgerController {

    @Autowired
    private TalentLedgerMapper talentLedgerMapper;

    @GetMapping("/list")
    public ResponseResult<List<TalentLedger>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String nineBox) {
        LambdaQueryWrapper<TalentLedger> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            qw.and(w -> w.like(TalentLedger::getEmployeeName, kw)
                    .or().like(TalentLedger::getEmployeeNo, kw));
        }
        if (StringUtils.hasText(department)) {
            qw.eq(TalentLedger::getDepartment, department.trim());
        }
        if (StringUtils.hasText(nineBox)) {
            qw.eq(TalentLedger::getNineBox, nineBox.trim());
        }
        qw.orderByDesc(TalentLedger::getId);
        return ResponseResult.success(talentLedgerMapper.selectList(qw));
    }

    @GetMapping("/{id}")
    public ResponseResult<TalentLedger> getById(@PathVariable Long id) {
        return ResponseResult.success(talentLedgerMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<TalentLedger> create(@RequestBody TalentLedger body) {
        LocalDateTime now = LocalDateTime.now();
        body.setId(null);
        body.setDeleted(0);
        body.setVersion(1);
        body.setCreateTime(now);
        body.setUpdateTime(now);
        talentLedgerMapper.insert(body);
        return ResponseResult.success(body);
    }

    @PutMapping("/{id}")
    public ResponseResult<TalentLedger> update(@PathVariable Long id, @RequestBody TalentLedger body) {
        body.setId(id);
        body.setUpdateTime(LocalDateTime.now());
        talentLedgerMapper.updateById(body);
        return ResponseResult.success(talentLedgerMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        talentLedgerMapper.deleteById(id);
        return ResponseResult.success();
    }
}
