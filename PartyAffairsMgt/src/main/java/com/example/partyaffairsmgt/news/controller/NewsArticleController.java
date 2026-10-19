package com.example.partyaffairsmgt.news.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.news.entity.NewsArticle;
import com.example.partyaffairsmgt.news.service.NewsArticleService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 稿件管理控制器（需求 7.2）。 */
@RestController
@RequestMapping("/api/party/news/article")
@CrossOrigin(origins = "*")
public class NewsArticleController {

    @Autowired
    private NewsArticleService articleService;

    @GetMapping("/list")
    public ResponseResult<List<NewsArticle>> list(
            @RequestParam(required = false) Long columnId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String creatorId) {
        return ResponseResult.success(articleService.list(columnId, status, keyword, creatorId));
    }

    @GetMapping("/{id}")
    public ResponseResult<NewsArticle> detail(@PathVariable Long id) {
        return ResponseResult.success(articleService.detail(id));
    }

    @PostMapping
    public ResponseResult<NewsArticle> saveDraft(@RequestBody NewsArticle article) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R13);
        return ResponseResult.success(articleService.saveDraft(article));
    }

    @PostMapping("/{id}/submit")
    public ResponseResult<NewsArticle> submit(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R13);
        return ResponseResult.success(articleService.submitForReview(id));
    }

    @PostMapping("/{id}/publish")
    public ResponseResult<Void> publish(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        articleService.publish(id);
        return ResponseResult.success(null);
    }

    @PostMapping("/{id}/offline")
    public ResponseResult<Void> offline(@PathVariable Long id, @RequestParam String reason) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        articleService.offline(id, reason);
        return ResponseResult.success(null);
    }

    @PostMapping("/{id}/withdraw")
    public ResponseResult<Void> withdraw(@PathVariable Long id, @RequestParam String reason) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03);
        articleService.withdraw(id, reason);
        return ResponseResult.success(null);
    }

    @PostMapping("/{id}/view")
    public ResponseResult<Void> incrementView(@PathVariable Long id) {
        articleService.incrementView(id);
        return ResponseResult.success(null);
    }
}
