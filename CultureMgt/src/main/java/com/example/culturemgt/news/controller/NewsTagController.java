package com.example.culturemgt.news.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.news.entity.NewsTag;
import com.example.culturemgt.news.service.NewsTagService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 子模块9 新闻标签管理（/api/culture/news/tag），维护为集团文化管理员专属。 */
@RestController
@RequestMapping("/api/culture/news/tag")
public class NewsTagController {

    private final NewsTagService tagService;

    public NewsTagController(NewsTagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/list")
    public ResponseResult<List<NewsTag>> list() {
        return ResponseResult.success(tagService.list());
    }

    @PostMapping
    public ResponseResult<NewsTag> save(@RequestBody NewsTag tag) {
        return ResponseResult.success(tagService.save(tag));
    }

    @PostMapping("/{id}/toggle")
    public ResponseResult<NewsTag> toggle(@PathVariable Long id) {
        return ResponseResult.success(tagService.toggle(id));
    }
}
