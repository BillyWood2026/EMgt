package com.example.partyaffairsmgt.news.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.news.entity.NewsRecommendation;
import com.example.partyaffairsmgt.news.service.NewsRecommendationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 置顶与推荐管理控制器（需求 7.4）。 */
@RestController
@RequestMapping("/api/party/news/recommendation")
@CrossOrigin(origins = "*")
public class NewsRecommendationController {

    @Autowired
    private NewsRecommendationService recommendationService;

    @GetMapping("/list")
    public ResponseResult<List<NewsRecommendation>> list(
            @RequestParam(required = false) Long columnId,
            @RequestParam(required = false) String recType,
            @RequestParam(required = false) String status) {
        return ResponseResult.success(recommendationService.list(columnId, recType, status));
    }

    @PostMapping
    public ResponseResult<NewsRecommendation> setRecommendation(@RequestBody NewsRecommendation rec) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(recommendationService.setRecommendation(rec));
    }

    @PostMapping("/{id}/cancel")
    public ResponseResult<Void> cancel(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        recommendationService.cancel(id);
        return ResponseResult.success(null);
    }

    @PostMapping("/expire")
    public ResponseResult<Integer> expireOverdue() {
        PartyRole.requireAny(PartyRole.R02);
        return ResponseResult.success(recommendationService.expireOverdue());
    }
}
