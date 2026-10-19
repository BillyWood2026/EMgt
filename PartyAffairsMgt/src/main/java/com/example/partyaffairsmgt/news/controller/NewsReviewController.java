package com.example.partyaffairsmgt.news.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.news.entity.NewsReview;
import com.example.partyaffairsmgt.news.service.NewsReviewService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 新闻审核发布控制器（需求 7.3）。 */
@RestController
@RequestMapping("/api/party/news/review")
@CrossOrigin(origins = "*")
public class NewsReviewController {

    @Autowired
    private NewsReviewService reviewService;

    @GetMapping("/list")
    public ResponseResult<List<NewsReview>> list(@RequestParam(required = false) Long articleId) {
        return ResponseResult.success(reviewService.list(articleId));
    }

    @PostMapping("/{articleId}")
    public ResponseResult<NewsReview> review(
            @PathVariable Long articleId,
            @RequestParam String reviewType,
            @RequestParam boolean pass,
            @RequestParam String opinion) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R03);
        return ResponseResult.success(reviewService.review(articleId, reviewType, pass, opinion));
    }

    @PostMapping("/{articleId}/emergency")
    public ResponseResult<Void> emergencyPublish(
            @PathVariable Long articleId, @RequestParam String reason) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03);
        reviewService.emergencyPublish(articleId, reason);
        return ResponseResult.success(null);
    }
}
