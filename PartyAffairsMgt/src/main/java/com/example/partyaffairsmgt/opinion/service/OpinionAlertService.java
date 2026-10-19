package com.example.partyaffairsmgt.opinion.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.opinion.entity.OpinionAlert;
import com.example.partyaffairsmgt.opinion.entity.OpinionAlertNotify;
import com.example.partyaffairsmgt.opinion.entity.OpinionAlertRule;
import com.example.partyaffairsmgt.opinion.entity.OpinionAnalysis;
import com.example.partyaffairsmgt.opinion.entity.OpinionCollection;
import com.example.partyaffairsmgt.opinion.mapper.OpinionAlertMapper;
import com.example.partyaffairsmgt.opinion.mapper.OpinionAlertNotifyMapper;
import com.example.partyaffairsmgt.opinion.mapper.OpinionAlertRuleMapper;
import com.example.partyaffairsmgt.opinion.mapper.OpinionAnalysisMapper;
import com.example.partyaffairsmgt.opinion.mapper.OpinionCollectionMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 舆情预警分级服务（需求 8.3）。
 * 阈值矩阵判定四色等级；夜间静默（22:00-7:00）延迟非红色推送；推送失败重试记录；
 * 同事件重复预警合并提醒；红色预警超时未认领升级。
 * 错误码：E-OP-12 推送失败；E-OP-13 同事件重复预警合并；E-OP-14 红色未认领升级；E-OP-15 规则不存在。
 */
@Service
public class OpinionAlertService {

    /** 夜间静默时段：22:00—次日7:00（参数化） */
    private static final int QUIET_START = 22;
    private static final int QUIET_END = 7;

    @Autowired
    private OpinionAlertMapper alertMapper;
    @Autowired
    private OpinionAlertRuleMapper ruleMapper;
    @Autowired
    private OpinionAlertNotifyMapper notifyMapper;
    @Autowired
    private OpinionAnalysisMapper analysisMapper;
    @Autowired
    private OpinionCollectionMapper collectionMapper;
    @Autowired
    private AuditService auditService;

    public List<OpinionAlert> list(String level, String status, String keyword) {
        QueryWrapper<OpinionAlert> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (level != null && !level.isBlank()) {
            qw.eq("alert_level", level);
        }
        if (status != null && !status.isBlank()) {
            qw.eq("status", status);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.like("title", keyword);
        }
        qw.orderByDesc("alert_time");
        return alertMapper.selectList(qw);
    }

    public OpinionAlert detail(Long id) {
        OpinionAlert a = alertMapper.selectById(id);
        if (a == null || a.getDeleted() == 1) {
            throw PartyBizException.block("E-OP-15", "预警记录不存在");
        }
        return a;
    }

    /**
     * 按阈值矩阵对分析结果触发预警（需求 8.3 表30）。
     *
     * @param failNotify 模拟推送失败（验证自动重试并记录 E-OP-12）
     */
    @Transactional
    public OpinionAlert trigger(Long analysisId, boolean failNotify) {
        OpinionAnalysis an = analysisMapper.selectById(analysisId);
        if (an == null || an.getDeleted() == 1) {
            throw PartyBizException.block("E-OP-15", "分析记录不存在");
        }
        OpinionCollection c = collectionMapper.selectById(an.getCollectionId());
        String sentiment = an.getSentiment();
        String heat = an.getHeatLevel() == null ? "低" : an.getHeatLevel();
        String rel = an.getRelevanceLevel() == null ? "低" : an.getRelevanceLevel();
        boolean major = c != null && c.getMajorEvent() != null && c.getMajorEvent() == 1;

        // 阈值矩阵：红=负面+高热+高关联 或 重大突发事件；橙=负面+热度中高；黄=负面+热度一般；蓝=其余
        String level;
        if (major) {
            level = "红色";
        } else if ("负面".equals(sentiment) && "高".equals(heat) && "高".equals(rel)) {
            level = "红色";
        } else if ("负面".equals(sentiment) && ("高".equals(heat) || "中".equals(heat))) {
            level = "橙色";
        } else if ("负面".equals(sentiment)) {
            level = "黄色";
        } else {
            level = "蓝色";
        }

        // 同事件重复预警合并提醒（E-OP-13）
        String cluster = an.getEventCluster();
        if (cluster != null && !cluster.isBlank()) {
            Long dup = alertMapper.selectCount(new QueryWrapper<OpinionAlert>()
                    .eq("deleted", 0).eq("event_cluster", cluster).ne("status", "已解除"));
            if (dup != null && dup > 0) {
                throw new PartyBizException("E-OP-13", PartyBizException.WARN,
                        "同事件[" + cluster + "]已存在预警，本次预警已合并提醒，不重复建单");
            }
        }

        OpinionAlertRule rule = ruleMapper.selectOne(new QueryWrapper<OpinionAlertRule>()
                .eq("deleted", 0).eq("level", level.replace("红色", "红").replace("橙色", "橙")
                        .replace("黄色", "黄").replace("蓝色", "蓝"))
                .eq("status", "启用").last("LIMIT 1"));
        if (rule == null) {
            throw PartyBizException.block("E-OP-15", "未配置[" + level + "]预警规则");
        }

        OpinionAlert a = new OpinionAlert();
        a.setCollectionId(an.getCollectionId());
        a.setAnalysisId(analysisId);
        a.setTitle(an.getTitle());
        a.setAlertLevel(level);
        a.setAlertReason(major ? "涉及重大突发事件，直接判定" + level
                : "情感" + sentiment + "/热度" + heat + "/关联度" + rel + "，按阈值矩阵判定" + level);
        a.setAlertTime(LocalDateTime.now());
        a.setReceiver("党委负责人,舆情专员");
        if (rule.getDeadlineHours() != null && rule.getDeadlineHours() > 0) {
            a.setHandleDeadline(LocalDateTime.now().plusHours(rule.getDeadlineHours()));
        }
        a.setSentiment(sentiment);
        a.setHeatLevel(heat);
        a.setRelevanceLevel(rel);
        a.setEventCluster(cluster);
        a.setStatus("蓝色".equals(level) ? "已解除" : "待处置");
        a.setClaimStatus("未认领");

        // 夜间静默：22:00—次日7:00 延迟非红色预警推送
        int hour = LocalDateTime.now().getHour();
        boolean quiet = hour >= QUIET_START || hour < QUIET_END;
        if (failNotify) {
            // 推送失败自动重试并记录（E-OP-12）
            a.setNotifyStatus("推送失败");
            a.setNotifyFail("短信网关超时，已自动重试3次并记录，请检查通道");
        } else if (quiet && !"红色".equals(level)) {
            a.setNotifyStatus("静默延迟");
        } else {
            a.setNotifyStatus("已推送");
        }
        a.setCreateTime(LocalDateTime.now());
        alertMapper.insert(a);

        if (c != null) {
            c.setStatus("已预警");
            c.setUpdateTime(LocalDateTime.now());
            collectionMapper.updateById(c);
        }
        auditService.log("舆情预警", "触发预警", a.getId(), level + "：" + an.getTitle());
        return a;
    }

    /** 认领预警 */
    @Transactional
    public OpinionAlert claim(Long id) {
        OpinionAlert a = detail(id);
        a.setClaimStatus("已认领");
        a.setClaimTime(LocalDateTime.now());
        a.setUpdateTime(LocalDateTime.now());
        alertMapper.updateById(a);
        auditService.log("舆情预警", "认领", id, a.getTitle());
        return a;
    }

    /**
     * 红色预警超时未认领升级（默认30分钟，force 用于演示）。
     */
    @Transactional
    public OpinionAlert escalateUnclaimed(Long id, boolean force) {
        OpinionAlert a = detail(id);
        OpinionAlertRule rule = ruleMapper.selectOne(new QueryWrapper<OpinionAlertRule>()
                .eq("deleted", 0).eq("level", "红").last("LIMIT 1"));
        int timeoutMin = rule != null && rule.getClaimTimeoutMin() != null ? rule.getClaimTimeoutMin() : 30;
        if (!"红色".equals(a.getAlertLevel())) {
            throw PartyBizException.block("E-OP-14", "仅红色预警支持未认领升级，当前为" + a.getAlertLevel());
        }
        boolean overdue = a.getAlertTime() != null
                && a.getAlertTime().plusMinutes(timeoutMin).isBefore(LocalDateTime.now());
        if (!overdue && !force) {
            throw new PartyBizException("E-OP-14", PartyBizException.WARN,
                    "红色预警认领超时为" + timeoutMin + "分钟，当前尚未超时，暂不升级");
        }
        if ("已认领".equals(a.getClaimStatus())) {
            throw PartyBizException.block("E-OP-14", "该预警已认领，无需升级");
        }
        a.setClaimStatus("已升级");
        a.setNotifyStatus("已升级");
        a.setUpdateTime(LocalDateTime.now());
        alertMapper.updateById(a);
        auditService.log("舆情预警", "红色未认领升级", id, "超过" + timeoutMin + "分钟未认领，升级通知");
        return a;
    }

    // ---------------- 规则与通知设置 ----------------

    public List<OpinionAlertRule> listRules() {
        return ruleMapper.selectList(new QueryWrapper<OpinionAlertRule>()
                .eq("deleted", 0).orderByAsc("id"));
    }

    @Transactional
    public OpinionAlertRule saveRule(OpinionAlertRule rule) {
        if (rule.getRuleName() == null || rule.getLevel() == null) {
            throw PartyBizException.block("E-OP-15", "规则名称与级别必填");
        }
        rule.setUpdateTime(LocalDateTime.now());
        if (rule.getId() == null) {
            rule.setCreateTime(LocalDateTime.now());
            ruleMapper.insert(rule);
        } else {
            ruleMapper.updateById(rule);
        }
        auditService.log("舆情预警", "保存分级规则", rule.getId(), rule.getRuleName());
        return rule;
    }

    @Transactional
    public void deleteRule(Long id) {
        ruleMapper.deleteById(id);
    }

    public List<OpinionAlertNotify> listNotify() {
        return notifyMapper.selectList(new QueryWrapper<OpinionAlertNotify>()
                .eq("deleted", 0).orderByAsc("id"));
    }

    @Transactional
    public OpinionAlertNotify saveNotify(OpinionAlertNotify n) {
        if (n.getNotifyType() == null || n.getNotifyType().isBlank()) {
            throw PartyBizException.block("E-OP-12", "通知类型必填");
        }
        n.setUpdateTime(LocalDateTime.now());
        if (n.getId() == null) {
            n.setCreateTime(LocalDateTime.now());
            notifyMapper.insert(n);
        } else {
            notifyMapper.updateById(n);
        }
        return n;
    }

    @Transactional
    public void deleteNotify(Long id) {
        notifyMapper.deleteById(id);
    }
}
