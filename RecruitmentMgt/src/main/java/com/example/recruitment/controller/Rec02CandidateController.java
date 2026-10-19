package com.example.recruitment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.dto.ResponseResult;
import com.example.recruitment.entity.Candidate;
import com.example.recruitment.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 候选人（人才池 rec02）REST 接口，契约对齐前端 Candidate.vue：
 * base /api/recruitment/rec02
 */
@RestController
@RequestMapping("/api/recruitment/rec02")
@CrossOrigin(origins = "*")
public class Rec02CandidateController {

    @Autowired
    private CandidateService candidateService;

    /**
     * 列表查询。可选参数：name（姓名，模糊）、position（应聘岗位，精确）、
     * source（来源，精确）；page/size 忽略，直接返回数组。
     */
    @GetMapping("/list")
    public ResponseResult<List<Candidate>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String source) {
        LambdaQueryWrapper<Candidate> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), Candidate::getName, name)
                .eq(StringUtils.hasText(position), Candidate::getPosition, position)
                .eq(StringUtils.hasText(source), Candidate::getSource, source)
                .orderByDesc(Candidate::getCreateTime)
                .orderByDesc(Candidate::getId);
        return ResponseResult.success(candidateService.list(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<Candidate> getById(@PathVariable Long id) {
        return ResponseResult.success(candidateService.getById(id));
    }

    @PostMapping
    public ResponseResult<Candidate> create(@RequestBody Candidate entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setId(null);
        if (!StringUtils.hasText(entity.getCurrentStage())) {
            entity.setCurrentStage("简历筛选");
        }
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        candidateService.save(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody Candidate entity) {
        entity.setId(id);
        Candidate existing = candidateService.getById(id);
        if (existing != null) {
            entity.setVersion(existing.getVersion());
        }
        entity.setUpdateTime(LocalDateTime.now());
        return ResponseResult.success(candidateService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(candidateService.removeById(id));
    }
}
