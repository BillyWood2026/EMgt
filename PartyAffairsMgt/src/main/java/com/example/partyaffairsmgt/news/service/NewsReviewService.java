package com.example.partyaffairsmgt.news.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.news.entity.NewsArticle;
import com.example.partyaffairsmgt.news.entity.NewsColumn;
import com.example.partyaffairsmgt.news.entity.NewsReview;
import com.example.partyaffairsmgt.news.mapper.NewsArticleMapper;
import com.example.partyaffairsmgt.news.mapper.NewsColumnMapper;
import com.example.partyaffairsmgt.news.mapper.NewsReviewMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 新闻审核发布服务（需求 7.3）。
 * 编辑提交→初审→复审→终审→发布；角色分离、互斥校验、紧急通道。
 */
@Service
public class NewsReviewService {

    @Autowired
    private NewsReviewMapper reviewMapper;
    @Autowired
    private NewsArticleMapper articleMapper;
    @Autowired
    private NewsColumnMapper columnMapper;
    @Autowired
    private AuditService auditService;

    /** 审核记录列表 */
    public List<NewsReview> list(Long articleId) {
        QueryWrapper<NewsReview> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (articleId != null) qw.eq("article_id", articleId);
        qw.orderByAsc("create_time");
        return reviewMapper.selectList(qw);
    }

    /** 审核（初审/复审/终审） */
    @Transactional
    public NewsReview review(Long articleId, String reviewType, boolean pass, String opinion) {
        if (opinion == null || opinion.isBlank()) {
            throw PartyBizException.block("E-NEWS-09", "审核意见不能为空");
        }
        NewsArticle article = articleMapper.selectById(articleId);
        if (article == null || article.getDeleted() == 1) {
            throw PartyBizException.block("E-NEWS-03", "稿件不存在");
        }
        if (!"待审".equals(article.getStatus())) {
            throw PartyBizException.block("E-NEWS-10", "稿件不在待审状态");
        }
        // 互斥校验：审核人不得审核本人稿件
        String operator = CurrentHeader.operator();
        if (operator.equals(article.getCreatorId())) {
            throw PartyBizException.block("E-NEWS-11", "审核人不得审核本人稿件");
        }
        // 校验审核级别顺序
        if (!reviewType.equals(article.getCurrentReviewLevel())) {
            throw PartyBizException.block("E-NEWS-12",
                    "当前应进行[" + article.getCurrentReviewLevel() + "]，不能执行[" + reviewType + "]");
        }

        NewsReview review = new NewsReview();
        review.setArticleId(articleId);
        review.setArticleTitle(article.getTitle());
        review.setReviewLevel(reviewType);
        review.setReviewType(reviewType);
        review.setReviewer(operator);
        review.setReviewTime(LocalDateTime.now());
        review.setResult(pass ? "通过" : "驳回");
        review.setOpinion(opinion);
        review.setCreateTime(LocalDateTime.now());
        reviewMapper.insert(review);

        if (pass) {
            // 推进到下一级
            NewsColumn col = columnMapper.selectById(article.getColumnId());
            String approveLevel = col != null && col.getApproveLevel() != null ? col.getApproveLevel() : "一审";
            String nextLevel = nextLevel(reviewType, approveLevel);
            if (nextLevel == null) {
                // 终审通过，发布
                article.setStatus("已发布");
                article.setPublishTime(LocalDateTime.now());
                article.setCurrentReviewLevel("已完成");
            } else {
                article.setCurrentReviewLevel(nextLevel);
            }
        } else {
            article.setStatus("已退回");
            article.setCurrentReviewLevel(null);
        }
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.updateById(article);

        auditService.log("新闻审核", reviewType + (pass ? "通过" : "驳回"), articleId,
                article.getTitle() + "，意见：" + opinion);
        return review;
    }

    /** 紧急发布（事后补审） */
    @Transactional
    public void emergencyPublish(Long articleId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw PartyBizException.block("E-NEWS-13", "紧急发布原因必填");
        }
        NewsArticle article = articleMapper.selectById(articleId);
        if (article == null) throw PartyBizException.block("E-NEWS-03", "稿件不存在");
        article.setStatus("已发布");
        article.setPublishTime(LocalDateTime.now());
        article.setCurrentReviewLevel("紧急发布待补审");
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.updateById(article);

        NewsReview review = new NewsReview();
        review.setArticleId(articleId);
        review.setArticleTitle(article.getTitle());
        review.setReviewType("紧急发布");
        review.setReviewer(CurrentHeader.operator());
        review.setReviewTime(LocalDateTime.now());
        review.setResult("通过");
        review.setOpinion("紧急发布，事后补审。原因：" + reason);
        review.setCreateTime(LocalDateTime.now());
        reviewMapper.insert(review);
        auditService.log("新闻审核", "紧急发布", articleId, reason);
    }

    private String nextLevel(String current, String approveLevel) {
        if ("一审".equals(approveLevel)) return null;
        if ("初审".equals(current)) return "二审".equals(approveLevel) ? null : "复审";
        if ("复审".equals(current)) return "终审";
        return null;
    }
}
