package com.example.partyaffairsmgt.news.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.news.entity.NewsArticle;
import com.example.partyaffairsmgt.news.entity.NewsPublish;
import com.example.partyaffairsmgt.news.mapper.NewsArticleMapper;
import com.example.partyaffairsmgt.news.mapper.NewsPublishMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 多端同步服务（需求 7.6）。
 * 同步至官网/公众号/移动端，失败重试3次，仍失败告警。
 */
@Service
public class NewsPublishService {

    private static final int MAX_RETRY = 3;

    @Autowired
    private NewsPublishMapper publishMapper;
    @Autowired
    private NewsArticleMapper articleMapper;
    @Autowired
    private AuditService auditService;

    /** 发布记录列表 */
    public List<NewsPublish> list(Long articleId, String status, String channelType) {
        QueryWrapper<NewsPublish> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (articleId != null) qw.eq("article_id", articleId);
        if (status != null && !status.isBlank()) qw.eq("status", status);
        if (channelType != null && !channelType.isBlank()) qw.eq("channel_type", channelType);
        qw.orderByDesc("create_time");
        return publishMapper.selectList(qw);
    }

    /** 同步到各端 */
    @Transactional
    public void syncToChannels(Long articleId) {
        NewsArticle article = articleMapper.selectById(articleId);
        if (article == null) throw PartyBizException.block("E-NEWS-03", "稿件不存在");
        if (!"已发布".equals(article.getStatus())) {
            throw PartyBizException.block("E-NEWS-17", "仅已发布稿件可同步到多端");
        }
        String[] channels = {"官网", "公众号", "移动端"};
        for (String ch : channels) {
            NewsPublish pub = new NewsPublish();
            pub.setArticleId(articleId);
            pub.setArticleTitle(article.getTitle());
            pub.setChannel(ch + (ch.equals("官网") ? "站" : (ch.equals("公众号") ? "" : "APP")));
            pub.setChannelType(ch);
            pub.setPublisher(CurrentHeader.operator());
            pub.setStatus("待同步");
            pub.setRetryCount(0);
            pub.setCreateTime(LocalDateTime.now());
            publishMapper.insert(pub);
            // 模拟同步（实际应调用各端 API）
            doSync(pub);
        }
        auditService.log("新闻发布", "多端同步", articleId, article.getTitle());
    }

    /** 执行同步（模拟） */
    @Transactional
    public void doSync(NewsPublish pub) {
        try {
            // 模拟同步成功
            pub.setStatus("已同步");
            pub.setPublishTime(LocalDateTime.now());
            pub.setUrl("https://example.com/news/" + pub.getArticleId() + "?ch=" + pub.getChannelType());
        } catch (Exception e) {
            pub.setStatus("同步失败");
            pub.setLastError(e.getMessage());
            pub.setRetryCount((pub.getRetryCount() == null ? 0 : pub.getRetryCount()) + 1);
        }
        pub.setUpdateTime(LocalDateTime.now());
        publishMapper.updateById(pub);
    }

    /** 重试失败的同步 */
    @Transactional
    public int retryFailed() {
        List<NewsPublish> failed = publishMapper.selectList(
                new QueryWrapper<NewsPublish>()
                        .eq("status", "同步失败")
                        .eq("deleted", 0)
                        .lt("retry_count", MAX_RETRY));
        int count = 0;
        for (NewsPublish p : failed) {
            doSync(p);
            if ("已同步".equals(p.getStatus())) count++;
        }
        // 超过重试次数的告警
        List<NewsPublish> exhausted = publishMapper.selectList(
                new QueryWrapper<NewsPublish>()
                        .eq("status", "同步失败")
                        .ge("retry_count", MAX_RETRY)
                        .eq("deleted", 0));
        for (NewsPublish p : exhausted) {
            auditService.log("新闻发布", "同步失败告警", p.getId(),
                    p.getArticleTitle() + " 渠道:" + p.getChannel() + " 错误:" + p.getLastError());
        }
        return count;
    }
}
