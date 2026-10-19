package com.example.skillcert.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.example.skillcert.entity.Skill;
import com.example.skillcert.service.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 技能工种参考列表（skill-certification/SkillWarning.vue 下拉用）
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/skill-cert/skill")
public class SkillController {

    @Autowired
    private SkillService skillService;

    @GetMapping("/list")
    public ResponseResult<IPage<Skill>> list(@RequestParam(defaultValue = "1") long current,
                                             @RequestParam(defaultValue = "20") long size) {
        return ResponseResult.success(skillService.pageList(current, size));
    }

    // ---- 标准 CRUD ----

    @GetMapping("/{id}")
    public ResponseResult<Skill> getById(@PathVariable Long id) {
        return ResponseResult.success(skillService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody Skill skill) {
        return ResponseResult.success(skillService.save(skill));
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody Skill skill) {
        skill.setId(id);
        return ResponseResult.success(skillService.updateById(skill));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(skillService.removeById(id));
    }
}
