package com.example.talent.controller;

import com.example.common.dto.ResponseResult;
import com.example.talent.entity.TalentAssessment;
import com.example.talent.service.TalentAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Talent Assessment Controller (人才测评)
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/talent/assessment")
public class TalentAssessmentController {

    @Autowired
    private TalentAssessmentService talentAssessmentService;

    @GetMapping({"", "/list"})
    public ResponseResult<List<TalentAssessment>> list() {
        return ResponseResult.success(talentAssessmentService.list());
    }

    @GetMapping("/{id}")
    public ResponseResult<TalentAssessment> getById(@PathVariable Long id) {
        return ResponseResult.success(talentAssessmentService.getById(id));
    }

    @PostMapping
    public ResponseResult<Void> create(@RequestBody TalentAssessment talentAssessment) {
        talentAssessmentService.save(talentAssessment);
        return ResponseResult.success();
    }

    @PutMapping("/{id}")
    public ResponseResult<Void> update(@PathVariable Long id, @RequestBody TalentAssessment talentAssessment) {
        talentAssessment.setId(id);
        talentAssessmentService.updateById(talentAssessment);
        return ResponseResult.success();
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        talentAssessmentService.removeById(id);
        return ResponseResult.success();
    }
}
