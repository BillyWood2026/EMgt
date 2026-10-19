package com.example.partyaffairsmgt.news.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.news.entity.NewsArticle;
import com.example.partyaffairsmgt.news.mapper.NewsArticleMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 稿件管理服务（需求 7.2）。
 * 敏感词检测、自动摘要、定时发布、状态机（草稿/待审/已发布/已退回/已下线）。
 */
@Service
public class NewsArticleService {

    /** 内置敏感词库（演示用） */
    private static final List<String> SENSITIVE_WORDS = List.of("反动", "暴乱", "颠覆", "邪教");

    @Autowired
    private NewsArticleMapper articleMapper;
    @Autowired
    private AuditService auditService;

    /** 稿件列表（按状态/栏目/关键词筛选） */
    public List<NewsArticle> list(Long columnId, String status, String keyword, String creatorId) {
        QueryWrapper<NewsArticle> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (columnId != null) qw.eq("column_id", columnId);
        if (status != null && !status.isBlank()) qw.eq("status", status);
        if (creatorId != null && !creatorId.isBlank()) qw.eq("creator_id", creatorId);
        if (keyword != null && !keyword.isBlank()) {
            qw.and(q -> q.like("title", keyword).or().like("author", keyword));
        }
        qw.orderByDesc("create_time");
        return articleMapper.selectList(qw);
    }

    /** 稿件详情 */
    public NewsArticle detail(Long id) {
        NewsArticle a = articleMapper.selectById(id);
        if (a == null || a.getDeleted() == 1) {
            throw PartyBizException.block("E-NEWS-03", "稿件不存在");
        }
        return a;
    }

    /** 保存草稿 */
    @Transactional
    public NewsArticle saveDraft(NewsArticle article) {
        if (article.getTitle() == null || article.getTitle().isBlank()) {
            throw PartyBizException.block("E-NEWS-03", "标题必填");
        }
        // 自动摘要
        if ((article.getSummary() == null || article.getSummary().isBlank())
                && article.getContent() != null && !article.getContent().isBlank()) {
            String text = stripHtml(article.getContent());
            article.setSummary(text.length() > 120 ? text.substring(0, 120) : text);
        }
        // 敏感词检测
        detectSensitive(article);
        article.setStatus("草稿");
        article.setCreatorId(CurrentHeader.operator());
        if (article.getId() == null) {
            article.setCreateTime(LocalDateTime.now());
            articleMapper.insert(article);
        } else {
            article.setUpdateTime(LocalDateTime.now());
            articleMapper.updateById(article);
        }
        return article;
    }

    /** 提交审核 */
    @Transactional
    public NewsArticle submitForReview(Long id) {
        NewsArticle a = getArticle(id);
        if (!"草稿".equals(a.getStatus()) && !"已退回".equals(a.getStatus())) {
            throw PartyBizException.block("E-NEWS-04", "仅草稿或已退回稿件可提交审核");
        }
        if (a.getContent() == null || a.getContent().isBlank()) {
            throw PartyBizException.block("E-NEWS-03", "正文必填");
        }
        if (a.getColumnId() == null) {
            throw PartyBizException.block("E-NEWS-03", "请选择栏目");
        }
        // 敏感词检测
        detectSensitive(a);
        if ("命中".equals(a.getSensitiveStatus())) {
            throw PartyBizException.block("E-NEWS-05",
                    "稿件命中敏感词：" + a.getSensitiveWords() + "，请处理后再提交");
        }
        // 确定审核级别（从栏目读取，currentReviewLevel 从初审开始）
        a.setCurrentReviewLevel("初审");
        a.setStatus("待审");
        a.setSubmitTime(LocalDateTime.now());
        a.setUpdateTime(LocalDateTime.now());
        articleMapper.updateById(a);
        auditService.log("新闻稿件", "提交审核", id, a.getTitle());
        return a;
    }

    /** 发布（终审通过后调用） */
    @Transactional
    public void publish(Long id) {
        NewsArticle a = getArticle(id);
        if (!"待审".equals(a.getStatus())) {
            throw PartyBizException.block("E-NEWS-06", "仅待审且已完成终审的稿件可发布");
        }
        if ("命中".equals(a.getSensitiveStatus())) {
            throw PartyBizException.block("E-NEWS-05", "敏感词未处理，禁止发布");
        }
        a.setStatus("已发布");
        a.setPublishTime(LocalDateTime.now());
        a.setUpdateTime(LocalDateTime.now());
        articleMapper.updateById(a);
        auditService.log("新闻稿件", "发布", id, a.getTitle());
    }

    /** 下线 */
    @Transactional
    public void offline(Long id, String reason) {
        NewsArticle a = getArticle(id);
        if (!"已发布".equals(a.getStatus())) {
            throw PartyBizException.block("E-NEWS-07", "仅已发布稿件可下线");
        }
        a.setStatus("已下线");
        a.setOfflineTime(LocalDateTime.now());
        a.setUpdateTime(LocalDateTime.now());
        articleMapper.updateById(a);
        auditService.log("新闻稿件", "下线", id, a.getTitle() + "，原因：" + reason);
    }

    /** 撤稿（需审批，简化为记录原因） */
    @Transactional
    public void withdraw(Long id, String reason) {
        if (reason == null || reason.isBlank()) {
            throw PartyBizException.block("E-NEWS-08", "撤稿原因必填");
        }
        NewsArticle a = getArticle(id);
        a.setStatus("已下线");
        a.setWithdrawReason(reason);
        a.setOfflineTime(LocalDateTime.now());
        a.setUpdateTime(LocalDateTime.now());
        articleMapper.updateById(a);
        auditService.log("新闻稿件", "撤稿", id, a.getTitle() + "，原因：" + reason);
    }

    /** 阅读量+1（防刷简化：直接计数） */
    @Transactional
    public void incrementView(Long id) {
        NewsArticle a = getArticle(id);
        a.setViewCount((a.getViewCount() == null ? 0 : a.getViewCount()) + 1);
        articleMapper.updateById(a);
    }

    private NewsArticle getArticle(Long id) {
        NewsArticle a = articleMapper.selectById(id);
        if (a == null || a.getDeleted() == 1) {
            throw PartyBizException.block("E-NEWS-03", "稿件不存在");
        }
        return a;
    }

    /** 敏感词检测 */
    private void detectSensitive(NewsArticle article) {
        String text = (article.getTitle() == null ? "" : article.getTitle())
                + (article.getContent() == null ? "" : stripHtml(article.getContent()));
        StringBuilder hit = new StringBuilder();
        for (String w : SENSITIVE_WORDS) {
            if (text.contains(w)) {
                if (hit.length() > 0) hit.append(",");
                hit.append(w);
            }
        }
        if (hit.length() > 0) {
            article.setSensitiveStatus("命中");
            article.setSensitiveWords(hit.toString());
        } else {
            article.setSensitiveStatus("已处理");
            article.setSensitiveWords(null);
        }
    }

    /** 去除 HTML 标签 */
    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]+>", "").replaceAll("&[a-z]+;", " ").trim();
    }
}
