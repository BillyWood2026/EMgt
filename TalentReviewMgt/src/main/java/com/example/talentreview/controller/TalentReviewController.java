package com.example.talentreview.controller;

import com.example.common.dto.ResponseResult;
import com.example.talentreview.entity.TalentReview;
import com.example.talentreview.service.TalentReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Talent Review Controller (人才评审)
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/talent/review")
public class TalentReviewController {

    @Autowired
    private TalentReviewService talentReviewService;

    @GetMapping({"", "/list"})
    public ResponseResult<List<TalentReview>> list() {
        return ResponseResult.success(talentReviewService.list());
    }

    @GetMapping("/{id}")
    public ResponseResult<TalentReview> getById(@PathVariable Long id) {
        return ResponseResult.success(talentReviewService.getById(id));
    }

    @PostMapping
    public ResponseResult<Void> create(@RequestBody TalentReview talentReview) {
        talentReviewService.save(talentReview);
        return ResponseResult.success();
    }

    @PutMapping("/{id}")
    public ResponseResult<Void> update(@PathVariable Long id, @RequestBody TalentReview talentReview) {
        talentReview.setId(id);
        talentReviewService.updateById(talentReview);
        return ResponseResult.success();
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        talentReviewService.removeById(id);
        return ResponseResult.success();
    }
}
