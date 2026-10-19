package com.example.partyaffairsmgt.opinion.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.opinion.entity.OpinionAnalysis;
import com.example.partyaffairsmgt.opinion.entity.OpinionCollection;
import com.example.partyaffairsmgt.opinion.entity.OpinionKeyword;
import com.example.partyaffairsmgt.opinion.mapper.OpinionAnalysisMapper;
import com.example.partyaffairsmgt.opinion.mapper.OpinionCollectionMapper;
import com.example.partyaffairsmgt.opinion.mapper.OpinionKeywordMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 舆情分析服务（需求 8.2）。
 * 情感分析（词典+模型分值-1至1，<-0.2判负面）、热度指数（加权+时间衰减）、
 * 关联度评分（关键词命中数量与位置加权）、事件聚类（标题相似度与URL同源）、批量标注、人工改判。
 * 错误码：E-OP-05 舆情不存在；E-OP-06 人工改判需理由。
 */
@Service
public class OpinionAnalysisService {

    /** 情感判定阈值（参数化） */
    public static final double NEGATIVE_THRESHOLD = -0.2;
    public static final double POSITIVE_THRESHOLD = 0.2;
    /** 热度权重 H=α×转发+β×评论+γ×点赞+δ×(阅读/100)，时间衰减因子 e^(-λΔt) */
    private static final double W_FWD = 0.4;
    private static final double W_CMT = 0.3;
    private static final double W_LIKE = 0.2;
    private static final double W_READ = 0.1;
    private static final double LAMBDA_PER_HOUR = 0.01;
    /** 衰减时间窗上限（小时） */
    private static final double MAX_DECAY_HOURS = 72;
    /** 热度等级阈值 */
    private static final double HEAT_HIGH = 300;
    private static final double HEAT_MID = 80;

    private static final List<String> NEG_WORDS = List.of(
            "投诉无果", "怀疑", "缺位", "推诿", "欠薪", "事故", "爆料", "扰民", "违规", "谣言", "整改", "致歉", "慢", "误读");
    private static final List<String> POS_WORDS = List.of(
            "好评", "成效", "稳岗", "先进", "表彰", "提升", "满意", "致谢", "获", "创新");

    @Autowired
    private OpinionAnalysisMapper analysisMapper;
    @Autowired
    private OpinionCollectionMapper collectionMapper;
    @Autowired
    private OpinionKeywordMapper keywordMapper;
    @Autowired
    private AuditService auditService;

    /** 舆情列表（按分类、情感、热度、来源筛选排序） */
    public List<OpinionAnalysis> list(String sentiment, String heatLevel, String eventCluster, String keyword) {
        QueryWrapper<OpinionAnalysis> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (sentiment != null && !sentiment.isBlank()) {
            qw.eq("sentiment", sentiment);
        }
        if (heatLevel != null && !heatLevel.isBlank()) {
            qw.eq("heat_level", heatLevel);
        }
        if (eventCluster != null && !eventCluster.isBlank()) {
            qw.eq("event_cluster", eventCluster);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.like("title", keyword);
        }
        qw.orderByDesc("heat_value");
        return analysisMapper.selectList(qw);
    }

    public OpinionAnalysis detail(Long id) {
        OpinionAnalysis a = analysisMapper.selectById(id);
        if (a == null || a.getDeleted() == 1) {
            throw PartyBizException.block("E-OP-05", "舆情分析记录不存在");
        }
        return a;
    }

    /**
     * 对采集信息执行自动分析：情感分值 + 热度指数 + 关联度 + 事件聚类。
     */
    @Transactional
    public OpinionAnalysis analyze(Long collectionId) {
        OpinionCollection c = collectionMapper.selectById(collectionId);
        if (c == null || c.getDeleted() == 1) {
            throw PartyBizException.block("E-OP-05", "采集信息不存在");
        }
        String title = c.getTitle() == null ? "" : c.getTitle();
        String content = c.getContent() == null ? "" : c.getContent();

        // 1. 情感分析：词典命中计分，区间-1至1
        int neg = countHits(title + content, NEG_WORDS);
        int pos = countHits(title + content, POS_WORDS);
        double score = (neg + pos) == 0 ? 0 : (double) (pos - neg) / (pos + neg);
        String sentiment = score < NEGATIVE_THRESHOLD ? "负面" : (score > POSITIVE_THRESHOLD ? "正面" : "中性");

        // 2. 热度指数：加权 + 时间衰减因子（越新热度越高）
        double fwd = c.getFwdCount() == null ? 0 : c.getFwdCount();
        double cmt = c.getCmtCount() == null ? 0 : c.getCmtCount();
        double like = c.getLikeCount() == null ? 0 : c.getLikeCount();
        double read = c.getReadCount() == null ? 0 : c.getReadCount();
        double hours = c.getCollectTime() == null ? 0
                : Math.min(Duration.between(c.getCollectTime(), LocalDateTime.now()).toHours(), MAX_DECAY_HOURS);
        double decay = Math.exp(-LAMBDA_PER_HOUR * hours);
        double heat = (W_FWD * fwd + W_CMT * cmt + W_LIKE * like + W_READ * (read / 100.0)) * decay;
        String heatLevel = heat >= HEAT_HIGH ? "高" : (heat >= HEAT_MID ? "中" : "低");

        // 3. 关联度评分：命中关键词库（标题×2、正文×1）加权
        int relevance = 0;
        for (OpinionKeyword kw : keywordMapper.selectList(
                new QueryWrapper<OpinionKeyword>().eq("deleted", 0).eq("status", "启用"))) {
            if (kw.getName() == null || kw.getName().isBlank()) {
                continue;
            }
            if (title.contains(kw.getName())) {
                relevance += 2;
            }
            if (content.contains(kw.getName())) {
                relevance += 1;
            }
        }
        String relevanceLevel = relevance >= 4 ? "高" : (relevance >= 2 ? "中" : "低");

        // 4. 事件聚类：标题相似度 或 URL同源 归并
        String cluster = resolveCluster(c, title);

        OpinionAnalysis a = new OpinionAnalysis();
        a.setCollectionId(collectionId);
        a.setTitle(c.getTitle());
        a.setSentiment(sentiment);
        a.setSentimentScore(BigDecimal.valueOf(score).setScale(3, RoundingMode.HALF_UP));
        a.setHeatLevel(heatLevel);
        a.setHeatValue(BigDecimal.valueOf(heat).setScale(1, RoundingMode.HALF_UP));
        a.setRelevanceScore(relevance);
        a.setRelevanceLevel(relevanceLevel);
        a.setKeywords(c.getKeywords());
        a.setEventCluster(cluster);
        a.setMarkResult("待核实");
        a.setAnalyst("系统自动");
        a.setAnalyzeTime(LocalDateTime.now());
        a.setSummary("情感" + sentiment + "（分值" + a.getSentimentScore() + "），热度" + heatLevel
                + "（" + a.getHeatValue() + "），关联度" + relevanceLevel + "，聚类[" + cluster + "]。");
        a.setCreateTime(LocalDateTime.now());
        analysisMapper.insert(a);

        c.setStatus("已分析");
        c.setClusterId(cluster);
        c.setUpdateTime(LocalDateTime.now());
        collectionMapper.updateById(c);
        auditService.log("舆情分析", "自动分析", a.getId(), c.getTitle());
        return a;
    }

    /** 人工改判情感（需理由，E-OP-06） */
    @Transactional
    public OpinionAnalysis reJudge(Long id, String sentiment, String reason) {
        if (reason == null || reason.isBlank()) {
            throw PartyBizException.block("E-OP-06", "人工改判必须填写改判理由");
        }
        if (!List.of("正面", "负面", "中性").contains(sentiment)) {
            throw PartyBizException.block("E-OP-06", "改判情感仅支持：正面/负面/中性");
        }
        OpinionAnalysis a = detail(id);
        a.setManualSentiment(sentiment);
        a.setJudgeReason(reason);
        a.setSentiment(sentiment);
        a.setUpdateTime(LocalDateTime.now());
        analysisMapper.updateById(a);
        auditService.log("舆情分析", "人工改判", id, "改判为" + sentiment + "：" + reason);
        return a;
    }

    /** 批量标注：属实/不属实/待核实 */
    @Transactional
    public int batchMark(List<Long> ids, String markResult) {
        if (!List.of("属实", "不属实", "待核实").contains(markResult)) {
            throw PartyBizException.block("E-OP-06", "标注仅支持：属实/不属实/待核实");
        }
        int n = 0;
        for (Long id : ids) {
            OpinionAnalysis a = analysisMapper.selectById(id);
            if (a != null && a.getDeleted() == 0) {
                a.setMarkResult(markResult);
                a.setUpdateTime(LocalDateTime.now());
                analysisMapper.updateById(a);
                n++;
            }
        }
        auditService.log("舆情分析", "批量标注", null, markResult + "，共" + n + "条");
        return n;
    }

    /** 保存（人工新增分析记录） */
    @Transactional
    public OpinionAnalysis save(OpinionAnalysis a) {
        if (a.getCollectionId() == null) {
            throw PartyBizException.block("E-OP-05", "必须关联采集信息");
        }
        if (a.getId() == null) {
            a.setAnalyst(CurrentHeader.operator());
            a.setAnalyzeTime(LocalDateTime.now());
            a.setCreateTime(LocalDateTime.now());
            analysisMapper.insert(a);
        } else {
            a.setUpdateTime(LocalDateTime.now());
            analysisMapper.updateById(a);
        }
        return a;
    }

    /** 事件聚类：标题二元组相似度≥1/3 或 URL同源 → 归并已有聚类 */
    private String resolveCluster(OpinionCollection c, String title) {
        if (c.getClusterId() != null && !c.getClusterId().isBlank()) {
            return c.getClusterId();
        }
        String host = host(c.getUrl());
        List<OpinionCollection> others = collectionMapper.selectList(new QueryWrapper<OpinionCollection>()
                .eq("deleted", 0).ne("id", c.getId()).isNotNull("cluster_id"));
        for (OpinionCollection o : others) {
            boolean sameHost = host != null && host.equals(host(o.getUrl()));
            if (sameHost || similar(title, o.getTitle())) {
                return o.getClusterId();
            }
        }
        return "CLUSTER-" + c.getId();
    }

    private boolean similar(String a, String b) {
        Set<String> ga = grams(a);
        Set<String> gb = grams(b);
        if (ga.isEmpty() || gb.isEmpty()) {
            return false;
        }
        long shared = ga.stream().filter(gb::contains).count();
        return shared * 3 >= Math.min(ga.size(), gb.size());
    }

    private Set<String> grams(String s) {
        Set<String> set = new HashSet<>();
        if (s == null) {
            return set;
        }
        String t = s.replaceAll("[\\p{P}\\s]+", "");
        for (int i = 0; i + 2 <= t.length(); i++) {
            set.add(t.substring(i, i + 2));
        }
        return set;
    }

    private String host(String url) {
        if (url == null) {
            return null;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("https?://([^/]+)").matcher(url);
        return m.find() ? m.group(1) : null;
    }

    private int countHits(String text, List<String> words) {
        int n = 0;
        for (String w : words) {
            int idx = text.indexOf(w);
            while (idx >= 0) {
                n++;
                idx = text.indexOf(w, idx + w.length());
            }
        }
        return n;
    }
}
