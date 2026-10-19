package com.example.recruitment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.dto.ResponseResult;
import com.example.recruitment.entity.Interview;
import com.example.recruitment.service.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

/**
 * 面试管理 REST 接口，契约对齐前端 Interview.vue：
 * base /api/recruitment/interview
 */
@RestController
@RequestMapping("/api/recruitment/interview")
@CrossOrigin(origins = "*")
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    /**
     * 列表查询。前端仅传 page/size（忽略，返回数组）；
     * 额外支持可选 candidateName（模糊）、stage（精确）、result（精确）。
     */
    @GetMapping("/list")
    public ResponseResult<List<Interview>> list(
            @RequestParam(required = false) String candidateName,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String result) {
        LambdaQueryWrapper<Interview> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(candidateName), Interview::getCandidateName, candidateName)
                .eq(StringUtils.hasText(stage), Interview::getStage, stage)
                .eq(StringUtils.hasText(result), Interview::getResult, result)
                .orderByDesc(Interview::getInterviewDate)
                .orderByDesc(Interview::getId);
        return ResponseResult.success(interviewService.list(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<Interview> getById(@PathVariable Long id) {
        return ResponseResult.success(interviewService.getById(id));
    }

    @PostMapping
    public ResponseResult<Interview> create(@RequestBody Interview entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setId(null);
        if (!StringUtils.hasText(entity.getInterviewNo())) {
            entity.setInterviewNo("IV-" + Year.now().getValue() + "-"
                    + String.format("%06d", System.currentTimeMillis() % 1_000_000));
        }
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        if (isPass(entity.getResult()) && entity.getValuesScore() != null && entity.getValuesScore() < 60) {
            return ResponseResult.error(400, "价值观得分低于60分，一票否决，不得判定为通过");
        }
        interviewService.save(entity);
        return ResponseResult.success(entity);
    }

    /** 是否判定为通过（排除“未通过”）。 */
    private boolean isPass(String result) {
        return StringUtils.hasText(result) && result.contains("通过") && !result.contains("未通过");
    }

    /**
     * 更新面试。前端“录入结果”仅提交 id/candidateName/result/comment，
     * MyBatis-Plus 默认忽略 null 字段，不会清空其它列；version 取库中现值。
     */
    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody Interview entity) {
        entity.setId(id);
        Interview existing = interviewService.getById(id);
        if (existing != null) {
            entity.setVersion(existing.getVersion());
        }
        // 价值观<60 一票否决：以本次提交分值为准，未提交则取库中现值。
        Integer values = entity.getValuesScore() != null
                ? entity.getValuesScore()
                : (existing != null ? existing.getValuesScore() : null);
        String effectiveResult = StringUtils.hasText(entity.getResult())
                ? entity.getResult()
                : (existing != null ? existing.getResult() : null);
        if (isPass(effectiveResult) && values != null && values < 60) {
            return ResponseResult.error(400, "价值观得分低于60分，一票否决，不得判定为通过");
        }
        entity.setUpdateTime(LocalDateTime.now());
        return ResponseResult.success(interviewService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(interviewService.removeById(id));
    }
}
