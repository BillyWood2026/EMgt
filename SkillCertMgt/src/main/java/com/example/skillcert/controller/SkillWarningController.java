package com.example.skillcert.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.common.dto.ResponseResult;
import com.example.skillcert.entity.SkillWarning;
import com.example.skillcert.service.SkillWarningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 技能预警 标准 CRUD
 * （原查询入口 /api/skill-cert/ledger/warning-list 等保持不变）
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/skill-cert/warning")
public class SkillWarningController {

    @Autowired
    private SkillWarningService skillWarningService;

    @GetMapping("/list")
    public ResponseResult<List<SkillWarning>> list() {
        return ResponseResult.success(
                skillWarningService.list(
                        Wrappers.<SkillWarning>lambdaQuery().orderByDesc(SkillWarning::getId)));
    }

    @GetMapping("/{id}")
    public ResponseResult<SkillWarning> getById(@PathVariable Long id) {
        return ResponseResult.success(skillWarningService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody SkillWarning warning) {
        return ResponseResult.success(skillWarningService.save(warning));
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody SkillWarning warning) {
        warning.setId(id);
        return ResponseResult.success(skillWarningService.updateById(warning));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(skillWarningService.removeById(id));
    }
}
