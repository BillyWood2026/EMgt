package com.example.skillcert.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.example.skillcert.entity.SkillApplication;
import com.example.skillcert.service.SkillApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 技能申报（skill-certification/SkillApply.vue）
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/skill-cert/application")
public class SkillApplicationController {

    @Autowired
    private SkillApplicationService skillApplicationService;

    @GetMapping("/list")
    public ResponseResult<IPage<SkillApplication>> list(@RequestParam(defaultValue = "1") long current,
                                                          @RequestParam(defaultValue = "10") long size) {
        return ResponseResult.success(skillApplicationService.pageApply(current, size));
    }

    @GetMapping("/{id}")
    public ResponseResult<SkillApplication> getById(@PathVariable Long id) {
        return ResponseResult.success(skillApplicationService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody SkillApplication application) {
        return ResponseResult.success(skillApplicationService.submitApplication(application));
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody SkillApplication application) {
        application.setId(id);
        return ResponseResult.success(skillApplicationService.updateById(application));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(skillApplicationService.removeById(id));
    }
}
