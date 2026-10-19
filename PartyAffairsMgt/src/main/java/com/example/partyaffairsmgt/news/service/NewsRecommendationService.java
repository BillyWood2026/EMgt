package com.example.partyaffairsmgt.news.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.news.entity.NewsArticle;
import com.example.partyaffairsmgt.news.entity.NewsRecommendation;
import com.example.partyaffairsmgt.news.mapper.NewsArticleMapper;
import com.example.partyaffairsmgt.news.mapper.NewsRecommendationMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 置顶与推荐管理服务（需求 7.4）。
 * 同一栏目置顶上限（默认3）、到期自动取消、时间重叠提示。
 */
@Service
public class NewsRecommendationService {

    private static final int TOP_LIMIT = 3;

    @Autowired
    private NewsRecommendationMapper recMapper;
    @Autowired
    private NewsArticleMapper articleMapper;
    @Autowired
    private AuditService auditService;

    /** 推荐列表 */
    public List<NewsRecommendation> list(Long columnId, String recType, String status) {
        QueryWrapper<NewsRecommendation> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (columnId != null) qw.eq("column_id", columnId);
        if (recType != null && !recType.isBlank()) qw.eq("rec_type", recType);
        if (status != null && !status.isBlank()) qw.eq("status", status);
        qw.orderByDesc("rank_score");
        return recMapper.selectList(qw);
    }

    /** 设置置顶/推荐 */
    @Transactional
    public NewsRecommendation setRecommendation(NewsRecommendation rec) {
        if (rec.getArticleId() == null) {
            throw PartyBizException.block("E-NEWS-14", "请选择稿件");
        }
        NewsArticle article = articleMapper.selectById(rec.getArticleId());
        if (article == null) throw PartyBizException.block("E-NEWS-03", "稿件不存在");
        rec.setArticleTitle(article.getTitle());
        if (rec.getColumnId() == null) rec.setColumnId(article.getColumnId());
        rec.setOperator(CurrentHeader.operator());
        rec.setStatus("生效中");
        if (rec.getStartTime() == null) rec.setStartTime(LocalDateTime.now());

        // 置顶数量上限校验
        if ("置顶".equals(rec.getRecType())) {
            Long currentCount = recMapper.selectCount(
                    new QueryWrapper<NewsRecommendation>()
                            .eq("column_id", rec.getColumnId())
                            .eq("rec_type", "置顶")
                            .eq("status", "生效中")
                            .eq("deleted", 0));
            if (currentCount >= TOP_LIMIT) {
                throw PartyBizException.block("E-NEWS-15",
                        "该栏目置顶数量已达上限(" + TOP_LIMIT + "条)，请先取消已有置顶");
            }
        }
        // 时间重叠提示（同一稿件同一类型不重复）
        Long overlap = recMapper.selectCount(
                new QueryWrapper<NewsRecommendation>()
                        .eq("article_id", rec.getArticleId())
                        .eq("rec_type", rec.getRecType())
                        .eq("status", "生效中")
                        .eq("deleted", 0));
        if (overlap > 0) {
            throw PartyBizException.block("E-NEWS-16", "该稿件已存在生效中的" + rec.getRecType() + "，时间重叠");
        }
        rec.setCreateTime(LocalDateTime.now());
        recMapper.insert(rec);

        // 同步稿件 is_top 字段
        if ("置顶".equals(rec.getRecType())) {
            article.setIsTop(1);
            article.setUpdateTime(LocalDateTime.now());
            articleMapper.updateById(article);
        }
        auditService.log("新闻推荐", "设置" + rec.getRecType(), rec.getId(), article.getTitle());
        return rec;
    }

    /** 取消推荐 */
    @Transactional
    public void cancel(Long id) {
        NewsRecommendation rec = recMapper.selectById(id);
        if (rec == null) throw PartyBizException.block("E-NEWS-14", "推荐记录不存在");
        rec.setStatus("已取消");
        rec.setUpdateTime(LocalDateTime.now());
        recMapper.updateById(rec);
        if ("置顶".equals(rec.getRecType())) {
            NewsArticle article = articleMapper.selectById(rec.getArticleId());
            if (article != null) {
                article.setIsTop(0);
                article.setUpdateTime(LocalDateTime.now());
                articleMapper.updateById(article);
            }
        }
        auditService.log("新闻推荐", "取消", id, rec.getArticleTitle());
    }

    /** 到期自动取消（定时任务调用） */
    @Transactional
    public int expireOverdue() {
        List<NewsRecommendation> expired = recMapper.selectList(
                new QueryWrapper<NewsRecommendation>()
                        .eq("status", "生效中")
                        .eq("deleted", 0)
                        .lt("end_time", LocalDateTime.now()));
        for (NewsRecommendation r : expired) {
            r.setStatus("已过期");
            r.setUpdateTime(LocalDateTime.now());
            recMapper.updateById(r);
            if ("置顶".equals(r.getRecType())) {
                NewsArticle a = articleMapper.selectById(r.getArticleId());
                if (a != null) { a.setIsTop(0); a.setUpdateTime(LocalDateTime.now()); articleMapper.updateById(a); }
            }
        }
        return expired.size();
    }
}
