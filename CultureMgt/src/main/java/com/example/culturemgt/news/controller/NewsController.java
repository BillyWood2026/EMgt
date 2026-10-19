package com.example.culturemgt.news.controller;

import com.example.culturemgt.asset.entity.CultureAsset;
import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.news.entity.News;
import com.example.culturemgt.news.entity.NewsApproval;
import com.example.culturemgt.news.entity.NewsVersion;
import com.example.culturemgt.news.service.NewsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 子模块9 新闻宣传管理（/api/culture/news）。
 * 前台：/public /{id} /view /like；后台：/list /todo /stats /save /submit /approve /offline 等。
 */
@RestController
@RequestMapping("/api/culture/news")
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    /** 后台稿件工作台列表 */
    @GetMapping("/list")
    public ResponseResult<List<News>> list(@RequestParam(required = false) String status,
                                           @RequestParam(required = false) String newsType,
                                           @RequestParam(required = false) String org,
                                           @RequestParam(required = false) String keyword) {
        return ResponseResult.success(newsService.list(status, newsType, org, keyword));
    }

    /** 前台公开列表：仅已发布，置顶优先+发布时间倒序 */
    @GetMapping("/public")
    public ResponseResult<List<News>> publicList(@RequestParam(required = false) String newsType,
                                                 @RequestParam(required = false) String tag,
                                                 @RequestParam(required = false) String keyword) {
        return ResponseResult.success(newsService.publicList(newsType, tag, keyword));
    }

    /** 统计分析 */
    @GetMapping("/stats")
    public ResponseResult<Map<String, Object>> stats() {
        return ResponseResult.success(newsService.stats());
    }

    /** 我的审批待办 */
    @GetMapping("/todo")
    public ResponseResult<List<News>> todo() {
        return ResponseResult.success(newsService.todo());
    }

    /** 版本快照历史 */
    @GetMapping("/versions/{newsId}")
    public ResponseResult<List<NewsVersion>> versions(@PathVariable Long newsId) {
        return ResponseResult.success(newsService.versions(newsId));
    }

    /** 审批流转记录 */
    @GetMapping("/approvals/{newsId}")
    public ResponseResult<List<NewsApproval>> approvals(@PathVariable Long newsId) {
        return ResponseResult.success(newsService.approvals(newsId));
    }

    /** 详情（含关联案例标题/榜样姓名） */
    @GetMapping("/{id}")
    public ResponseResult<Map<String, Object>> get(@PathVariable Long id) {
        return ResponseResult.success(newsService.get(id));
    }

    /** 新建/编辑（每次保存自动生成版本快照；重复标题预警需 confirmDuplicate=true 放行） */
    @PostMapping
    public ResponseResult<News> save(@RequestBody News news) {
        return ResponseResult.success(newsService.save(news));
    }

    /** 配图素材一键存入文化资产库 */
    @PostMapping("/material")
    public ResponseResult<CultureAsset> material(@RequestBody Map<String, String> body) {
        return ResponseResult.success(newsService.material(body));
    }

    /** 提交审批：草稿 → 待审核 */
    @PostMapping("/{id}/submit")
    public ResponseResult<News> submit(@PathVariable Long id) {
        return ResponseResult.success(newsService.submit(id));
    }

    /** 撤回：待审核 → 草稿 */
    @PostMapping("/{id}/withdraw")
    public ResponseResult<News> withdraw(@PathVariable Long id) {
        return ResponseResult.success(newsService.withdraw(id));
    }

    /** 审批：pass=true 通过（终审通过即发布）；pass=false 驳回（opinion 20-500字必填） */
    @PostMapping("/{id}/approve")
    public ResponseResult<News> approve(@PathVariable Long id,
                                        @RequestParam boolean pass,
                                        @RequestParam(required = false) String opinion) {
        return ResponseResult.success(newsService.approve(id, pass, opinion));
    }

    /** 下线归档（原因必填） */
    @PostMapping("/{id}/offline")
    public ResponseResult<News> offline(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseResult.success(newsService.offline(id, body == null ? null : body.get("reason")));
    }

    /** 阅读计数（channel=mobile 计移动端阅读） */
    @PostMapping("/{id}/view")
    public ResponseResult<News> view(@PathVariable Long id,
                                     @RequestParam(required = false) String channel) {
        return ResponseResult.success(newsService.view(id, channel));
    }

    /** 点赞 */
    @PostMapping("/{id}/like")
    public ResponseResult<News> like(@PathVariable Long id) {
        return ResponseResult.success(newsService.like(id));
    }

    /** 删除草稿 */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        newsService.delete(id);
        return ResponseResult.success(null);
    }
}
