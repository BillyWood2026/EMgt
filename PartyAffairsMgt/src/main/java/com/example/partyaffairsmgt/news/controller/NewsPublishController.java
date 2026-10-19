package com.example.partyaffairsmgt.news.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.news.entity.NewsPublish;
import com.example.partyaffairsmgt.news.service.NewsPublishService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 多端同步控制器（需求 7.6）。 */
@RestController
@RequestMapping("/api/party/news/publish")
@CrossOrigin(origins = "*")
public class NewsPublishController {

    @Autowired
    private NewsPublishService publishService;

    @GetMapping("/list")
    public ResponseResult<List<NewsPublish>> list(
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String channelType) {
        return ResponseResult.success(publishService.list(articleId, status, channelType));
    }

    @PostMapping("/sync/{articleId}")
    public ResponseResult<Void> syncToChannels(@PathVariable Long articleId) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        publishService.syncToChannels(articleId);
        return ResponseResult.success(null);
    }

    @PostMapping("/retry")
    public ResponseResult<Integer> retryFailed() {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(publishService.retryFailed());
    }
}
