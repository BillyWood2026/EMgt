package com.example.skillcert.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.example.skillcert.entity.SkillType;
import com.example.skillcert.service.SkillTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 工种配置（skill-certification/SkillStandard.vue）
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/skill-cert/standard")
public class SkillStandardController {

    @Autowired
    private SkillTypeService skillTypeService;

    /** 工种列表 + 健康检查 */
    @GetMapping("/list")
    public ResponseResult<IPage<SkillType>> list(@RequestParam(defaultValue = "1") long current,
                                                  @RequestParam(defaultValue = "20") long size,
                                                  @RequestParam(required = false) String certType,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(required = false) String skillLevel) {
        return ResponseResult.success(
                skillTypeService.pageList(current, size, certType, status, skillLevel));
    }

    @GetMapping("/{id}")
    public ResponseResult<SkillType> getById(@PathVariable Long id) {
        return ResponseResult.success(skillTypeService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody SkillType skillType) {
        return ResponseResult.success(skillTypeService.save(skillType));
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody SkillType skillType) {
        skillType.setId(id);
        return ResponseResult.success(skillTypeService.updateById(skillType));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(skillTypeService.removeById(id));
    }
}
