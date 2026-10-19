package com.example.partyaffairsmgt.opinion.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.opinion.entity.OpinionCollection;
import com.example.partyaffairsmgt.opinion.entity.OpinionCollectLog;
import com.example.partyaffairsmgt.opinion.entity.OpinionCollectTask;
import com.example.partyaffairsmgt.opinion.entity.OpinionKeyword;
import com.example.partyaffairsmgt.opinion.entity.OpinionSource;
import com.example.partyaffairsmgt.opinion.mapper.OpinionCollectionMapper;
import com.example.partyaffairsmgt.opinion.mapper.OpinionCollectLogMapper;
import com.example.partyaffairsmgt.opinion.mapper.OpinionCollectTaskMapper;
import com.example.partyaffairsmgt.opinion.mapper.OpinionKeywordMapper;
import com.example.partyaffairsmgt.opinion.mapper.OpinionSourceMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 舆情采集服务（需求 8.1）。
 * 关键词库、信源配置、采集任务执行：指纹去重、失败重试3次、配额限流、命中风暴检测。
 * 错误码：E-OP-01 命中风暴限流合并；E-OP-02 采集中断/失败告警；E-OP-03 配额超限；E-OP-04 URL指纹重复。
 */
@Service
public class OpinionCollectService {

    /** 命中风暴阈值（1小时内命中超过该值触发限流与合并提醒，默认50，参数化） */
    public static final int STORM_THRESHOLD = 50;
    /** 采集失败重试上限 */
    public static final int MAX_RETRY = 3;

    @Autowired
    private OpinionKeywordMapper keywordMapper;
    @Autowired
    private OpinionSourceMapper sourceMapper;
    @Autowired
    private OpinionCollectTaskMapper taskMapper;
    @Autowired
    private OpinionCollectLogMapper logMapper;
    @Autowired
    private OpinionCollectionMapper collectionMapper;
    @Autowired
    private AuditService auditService;

    // ---------------- 关键词库 ----------------

    public List<OpinionKeyword> listKeywords(String group) {
        QueryWrapper<OpinionKeyword> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (group != null && !group.isBlank()) {
            qw.eq("group_name", group);
        }
        qw.orderByDesc("weight");
        return keywordMapper.selectList(qw);
    }

    @Transactional
    public OpinionKeyword saveKeyword(OpinionKeyword kw) {
        if (kw.getName() == null || kw.getName().isBlank()) {
            throw PartyBizException.block("E-OP-04", "关键词必填");
        }
        if (kw.getWeight() == null) {
            kw.setWeight(5);
        }
        kw.setUpdateTime(LocalDateTime.now());
        if (kw.getId() == null) {
            kw.setCreateTime(LocalDateTime.now());
            keywordMapper.insert(kw);
        } else {
            keywordMapper.updateById(kw);
        }
        auditService.log("舆情采集", "保存关键词", kw.getId(), kw.getName());
        return kw;
    }

    @Transactional
    public void deleteKeyword(Long id) {
        keywordMapper.deleteById(id);
        auditService.log("舆情采集", "删除关键词", id, null);
    }

    // ---------------- 信源配置 ----------------

    public List<OpinionSource> listSources(String type) {
        QueryWrapper<OpinionSource> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (type != null && !type.isBlank()) {
            qw.eq("type", type);
        }
        qw.orderByAsc("id");
        return sourceMapper.selectList(qw);
    }

    @Transactional
    public OpinionSource saveSource(OpinionSource src) {
        if (src.getName() == null || src.getName().isBlank() || src.getType() == null || src.getType().isBlank()) {
            throw PartyBizException.block("E-OP-04", "信源名称与类型必填");
        }
        src.setUpdateTime(LocalDateTime.now());
        if (src.getId() == null) {
            src.setCreateTime(LocalDateTime.now());
            sourceMapper.insert(src);
        } else {
            sourceMapper.updateById(src);
        }
        auditService.log("舆情采集", "保存信源", src.getId(), src.getName());
        return src;
    }

    @Transactional
    public void deleteSource(Long id) {
        sourceMapper.deleteById(id);
        auditService.log("舆情采集", "删除信源", id, null);
    }

    // ---------------- 采集信息 ----------------

    /** 舆情列表（按来源、状态、关键词筛选） */
    public List<OpinionCollection> listCollections(String sourcePlatform, String status, String keyword) {
        QueryWrapper<OpinionCollection> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (sourcePlatform != null && !sourcePlatform.isBlank()) {
            qw.eq("source_platform", sourcePlatform);
        }
        if (status != null && !status.isBlank()) {
            qw.eq("status", status);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.and(q -> q.like("title", keyword).or().like("keywords", keyword));
        }
        qw.orderByDesc("collect_time");
        return collectionMapper.selectList(qw);
    }

    public OpinionCollection detailCollection(Long id) {
        OpinionCollection c = collectionMapper.selectById(id);
        if (c == null || c.getDeleted() == 1) {
            throw PartyBizException.block("E-OP-04", "采集信息不存在");
        }
        return c;
    }

    /** 手工登记舆情（URL指纹MD5去重，重复提示合并 E-OP-04） */
    @Transactional
    public OpinionCollection saveCollection(OpinionCollection c) {
        if (c.getTitle() == null || c.getTitle().isBlank()) {
            throw PartyBizException.block("E-OP-04", "舆情标题必填");
        }
        if (c.getUrl() != null && !c.getUrl().isBlank()) {
            c.setUrlMd5(md5(c.getUrl()));
            Long dup = collectionMapper.selectCount(new QueryWrapper<OpinionCollection>()
                    .eq("deleted", 0).eq("url_md5", c.getUrlMd5())
                    .ne(c.getId() == null ? "id" : "id", c.getId() == null ? -1 : c.getId()));
            if (dup != null && dup > 0) {
                throw PartyBizException.block("E-OP-04", "URL指纹重复，该舆情已存在，已按同源规则合并提示");
            }
        }
        if (c.getStatus() == null) {
            c.setStatus("待分析");
        }
        c.setUpdateTime(LocalDateTime.now());
        if (c.getId() == null) {
            c.setCreateTime(LocalDateTime.now());
            collectionMapper.insert(c);
        } else {
            collectionMapper.updateById(c);
        }
        auditService.log("舆情采集", "登记舆情", c.getId(), c.getTitle());
        return c;
    }

    @Transactional
    public void deleteCollection(Long id) {
        collectionMapper.deleteById(id);
    }

    // ---------------- 采集任务 ----------------

    public List<OpinionCollectTask> listTasks(String status) {
        QueryWrapper<OpinionCollectTask> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (status != null && !status.isBlank()) {
            qw.eq("status", status);
        }
        qw.orderByAsc("id");
        return taskMapper.selectList(qw);
    }

    @Transactional
    public OpinionCollectTask saveTask(OpinionCollectTask task) {
        if (task.getName() == null || task.getName().isBlank()) {
            throw PartyBizException.block("E-OP-04", "任务名称必填");
        }
        if (task.getQuota() == null) {
            task.setQuota(100);
        }
        task.setUpdateTime(LocalDateTime.now());
        if (task.getId() == null) {
            task.setCreateTime(LocalDateTime.now());
            taskMapper.insert(task);
        } else {
            taskMapper.updateById(task);
        }
        auditService.log("舆情采集", "保存采集任务", task.getId(), task.getName());
        return task;
    }

    @Transactional
    public void deleteTask(Long id) {
        taskMapper.deleteById(id);
        auditService.log("舆情采集", "删除采集任务", id, null);
    }

    /**
     * 执行一次采集（演示定时采集）。
     *
     * @param simulate 模拟命中数（演示风暴/限流用）；为空时按关键词数×2
     * @param fail     模拟采集失败（验证重试3次后告警）
     */
    @Transactional
    public Map<String, Object> run(Long taskId, Integer simulate, boolean fail) {
        OpinionCollectTask task = taskMapper.selectById(taskId);
        if (task == null || task.getDeleted() == 1) {
            throw PartyBizException.block("E-OP-02", "采集任务不存在或已中断");
        }
        if (!"运行中".equals(task.getStatus())) {
            throw PartyBizException.block("E-OP-02", "任务状态为[" + task.getStatus() + "]，已中断采集并告警");
        }
        int hits = simulate != null ? simulate
                : Math.max(task.getKeywordGroup() == null ? 0 : task.getKeywordGroup().split(",").length, 1) * 2;

        // 命中风暴检测：1小时内命中超过阈值 → 限流与合并提醒（E-OP-01）
        if (hits > STORM_THRESHOLD) {
            insertLog(task, hits, 0, 0, "限流合并", "E-OP-01 命中风暴，已限流并将重复内容合并");
            throw PartyBizException.block("E-OP-01",
                    "关键词命中风暴：本次命中" + hits + "条超过阈值" + STORM_THRESHOLD + "条，已限流并合并提醒");
        }
        // 配额超限限流（E-OP-03）
        if (task.getQuota() != null && hits > task.getQuota()) {
            insertLog(task, hits, 0, 0, "限流", "E-OP-03 超过单次采集配额" + task.getQuota());
            throw PartyBizException.block("E-OP-03",
                    "采集配额超限：命中" + hits + "条超过配额" + task.getQuota() + "条，已限流");
        }

        int retry = 0;
        if (fail) {
            // 采集失败自动重试3次，仍失败告警（E-OP-02）
            while (retry < MAX_RETRY) {
                retry++;
                insertLog(task, 0, 0, retry, "重试中", "采集失败，第" + retry + "次重试");
            }
            insertLog(task, 0, 0, retry, "失败", "E-OP-02 重试" + MAX_RETRY + "次仍失败，触发中断告警");
            task.setStatus("异常");
            task.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);
            throw PartyBizException.block("E-OP-02",
                    "任务[" + task.getName() + "]采集失败，自动重试" + MAX_RETRY + "次仍失败，已告警并置任务为异常");
        }

        // URL指纹（MD5）去重，演示最多落库3条样本
        int inserted = 0;
        int dedup = 0;
        int samples = Math.min(hits, 3);
        for (int i = 1; i <= samples; i++) {
            String url = "http://example.com/task/" + taskId + "/" + System.currentTimeMillis() + "/" + i;
            String md5 = md5(url);
            Long dup = collectionMapper.selectCount(new QueryWrapper<OpinionCollection>()
                    .eq("deleted", 0).eq("url_md5", md5));
            if (dup != null && dup > 0) {
                dedup++;
                continue;
            }
            OpinionCollection c = new OpinionCollection();
            c.setTitle("【" + task.getName() + "】第" + i + "条采集样本");
            c.setSource(task.getSources());
            c.setUrl(url);
            c.setUrlMd5(md5);
            c.setAuthor("采集器");
            c.setKeywords(task.getKeywordGroup());
            c.setContent("按关键词[" + task.getKeywordGroup() + "]以" + task.getMatchMode()
                    + "模式命中，URL指纹已去重。");
            c.setCollectTime(LocalDateTime.now());
            c.setSentimentHint("中性");
            c.setStatus("待分析");
            c.setSourcePlatform(task.getSources() == null ? null : task.getSources().split(",")[0]);
            c.setFwdCount(0);
            c.setCmtCount(0);
            c.setLikeCount(0);
            c.setReadCount(0);
            c.setMerged(0);
            c.setTaskId(taskId);
            c.setMajorEvent(0);
            c.setCreateTime(LocalDateTime.now());
            collectionMapper.insert(c);
            inserted++;
        }

        task.setCollectCount((task.getCollectCount() == null ? 0 : task.getCollectCount()) + hits);
        task.setLastCollectTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);

        insertLog(task, hits, inserted, 0, "成功", dedup > 0 ? "URL指纹重复" + dedup + "条已去重合并" : null);
        auditService.log("舆情采集", "执行采集任务", taskId, "命中" + hits + "，新增" + inserted);

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("taskId", taskId);
        r.put("hitCount", hits);
        r.put("newCount", inserted);
        r.put("dedupCount", dedup);
        return r;
    }

    public List<OpinionCollectLog> listLogs(Long taskId, String status) {
        QueryWrapper<OpinionCollectLog> qw = new QueryWrapper<>();
        if (taskId != null) {
            qw.eq("task_id", taskId);
        }
        if (status != null && !status.isBlank()) {
            qw.eq("status", status);
        }
        qw.orderByDesc("create_time");
        return logMapper.selectList(qw);
    }

    private void insertLog(OpinionCollectTask task, int hits, int newCount, int retry, String status, String errorMsg) {
        OpinionCollectLog log = new OpinionCollectLog();
        log.setTaskId(task.getId());
        log.setTaskName(task.getName());
        log.setCollectTime(LocalDateTime.now());
        log.setHitCount(hits);
        log.setNewCount(newCount);
        log.setRetryCount(retry);
        log.setStatus(status);
        log.setErrorMsg(errorMsg);
        log.setCreateTime(LocalDateTime.now());
        logMapper.insert(log);
    }

    private String md5(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(text.hashCode());
        }
    }
}
