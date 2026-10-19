package com.example.partyaffairsmgt.opinion.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.opinion.entity.OpinionAnalysis;
import com.example.partyaffairsmgt.opinion.entity.OpinionCollectLog;
import com.example.partyaffairsmgt.opinion.entity.OpinionCollection;
import com.example.partyaffairsmgt.opinion.entity.OpinionHandle;
import com.example.partyaffairsmgt.opinion.entity.OpinionReport;
import com.example.partyaffairsmgt.opinion.entity.OpinionReportTemplate;
import com.example.partyaffairsmgt.opinion.mapper.OpinionAnalysisMapper;
import com.example.partyaffairsmgt.opinion.mapper.OpinionCollectLogMapper;
import com.example.partyaffairsmgt.opinion.mapper.OpinionCollectionMapper;
import com.example.partyaffairsmgt.opinion.mapper.OpinionHandleMapper;
import com.example.partyaffairsmgt.opinion.mapper.OpinionReportMapper;
import com.example.partyaffairsmgt.opinion.mapper.OpinionReportTemplateMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.StringJoiner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 舆情报告服务（需求 8.5）。
 * 日报/周报/月报自动生成：总量、负面量、热点TOP10、处置闭环率、环比；
 * 数据源缺失标注说明（不生成虚假数据）。
 * 错误码：E-OP-18 报告不存在；E-OP-15 数据源缺失标注。
 */
@Service
public class OpinionReportService {

    @Autowired
    private OpinionReportMapper reportMapper;
    @Autowired
    private OpinionReportTemplateMapper templateMapper;
    @Autowired
    private OpinionCollectionMapper collectionMapper;
    @Autowired
    private OpinionAnalysisMapper analysisMapper;
    @Autowired
    private OpinionHandleMapper handleMapper;
    @Autowired
    private OpinionCollectLogMapper logMapper;
    @Autowired
    private AuditService auditService;

    public List<OpinionReport> list(String reportType, String status) {
        QueryWrapper<OpinionReport> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (reportType != null && !reportType.isBlank()) {
            qw.eq("report_type", reportType);
        }
        if (status != null && !status.isBlank()) {
            qw.eq("status", status);
        }
        qw.orderByDesc("create_time");
        return reportMapper.selectList(qw);
    }

    public OpinionReport detail(Long id) {
        OpinionReport r = reportMapper.selectById(id);
        if (r == null || r.getDeleted() == 1) {
            throw PartyBizException.block("E-OP-18", "舆情报告不存在");
        }
        return r;
    }

    public List<OpinionReportTemplate> listTemplates() {
        return templateMapper.selectList(new QueryWrapper<OpinionReportTemplate>()
                .eq("deleted", 0).orderByAsc("id"));
    }

    @Transactional
    public OpinionReportTemplate saveTemplate(OpinionReportTemplate t) {
        if (t.getName() == null || t.getReportType() == null) {
            throw PartyBizException.block("E-OP-18", "模板名称与报告类型必填");
        }
        t.setUpdateTime(LocalDateTime.now());
        if (t.getId() == null) {
            t.setCreateTime(LocalDateTime.now());
            templateMapper.insert(t);
        } else {
            templateMapper.updateById(t);
        }
        return t;
    }

    @Transactional
    public void deleteTemplate(Long id) {
        templateMapper.deleteById(id);
    }

    /** 自动生成日报/周报/月报 */
    @Transactional
    public OpinionReport generate(String reportType, Long templateId) {
        LocalDate end = LocalDate.now();
        LocalDate start;
        String title;
        switch (reportType == null ? "日报" : reportType) {
            case "周报" -> {
                start = end.minusDays(7);
                title = "舆情周报（" + start + "至" + end + "）";
            }
            case "月报" -> {
                start = end.minusDays(30);
                title = "舆情月报（" + start + "至" + end + "）";
            }
            default -> {
                start = end;
                title = "舆情日报（" + end + "）";
            }
        }
        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();

        // 舆情总量与负面量
        List<OpinionCollection> colls = collectionMapper.selectList(new QueryWrapper<OpinionCollection>()
                .eq("deleted", 0).ge("collect_time", startTime).lt("collect_time", endTime));
        int total = colls.size();
        int negative = 0;
        for (OpinionCollection c : colls) {
            OpinionAnalysis an = analysisMapper.selectOne(new QueryWrapper<OpinionAnalysis>()
                    .eq("deleted", 0).eq("collection_id", c.getId()).last("LIMIT 1"));
            String s = an != null && an.getSentiment() != null ? an.getSentiment() : c.getSentimentHint();
            if ("负面".equals(s) || "负面".equals(an == null ? null : an.getManualSentiment())) {
                negative++;
            }
        }

        // 热点TOP10
        List<OpinionAnalysis> tops = analysisMapper.selectList(new QueryWrapper<OpinionAnalysis>()
                .eq("deleted", 0).ge("analyze_time", startTime).lt("analyze_time", endTime)
                .orderByDesc("heat_value").last("LIMIT 10"));
        StringJoiner joiner = new StringJoiner(";");
        int rank = 1;
        for (OpinionAnalysis t : tops) {
            joiner.add(rank + "." + t.getTitle() + "(热度" + t.getHeatValue() + ")");
            rank++;
        }

        // 处置闭环率（已归档/应处置）
        List<OpinionHandle> handles = handleMapper.selectList(new QueryWrapper<OpinionHandle>()
                .eq("deleted", 0).ge("create_time", startTime).lt("create_time", endTime));
        int due = handles.size();
        int closed = (int) handles.stream().filter(h -> "已归档".equals(h.getArchiveStatus())).count();
        BigDecimal closeRate = due == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(closed * 100.0 / due).setScale(2, RoundingMode.HALF_UP);

        // 环比：与上一等长周期总量比较
        LocalDateTime prevStart = startTime.minusDays(java.time.Duration.between(startTime, endTime).toDays());
        Long prevTotal = collectionMapper.selectCount(new QueryWrapper<OpinionCollection>()
                .eq("deleted", 0).ge("collect_time", prevStart).lt("collect_time", startTime));
        BigDecimal mom = (prevTotal == null || prevTotal == 0) ? null
                : BigDecimal.valueOf((total - prevTotal) * 100.0 / prevTotal).setScale(2, RoundingMode.HALF_UP);

        // 数据源缺失标注（E-OP-15）：采集中断/失败日志则标注说明，不生成虚假数据
        Long failLogs = logMapper.selectCount(new QueryWrapper<OpinionCollectLog>()
                .in("status", "失败", "中断告警", "限流", "限流合并")
                .ge("collect_time", startTime).lt("collect_time", endTime));
        String dataNote = (failLogs != null && failLogs > 0)
                ? "统计期内存在" + failLogs + "条采集失败/限流日志，相关时段数据可能缺失，已标注说明"
                : null;

        OpinionReport r = new OpinionReport();
        r.setReportNo("OP-" + end.toString().replace("-", "") + "-" + (reportType == null ? "日报" : reportType));
        r.setTitle(title);
        r.setReportType(reportType == null ? "日报" : reportType);
        r.setPeriodStart(start);
        r.setPeriodEnd(end);
        r.setContent(title + "：共采集舆情" + total + "条，负面" + negative + "条，热点TOP"
                + tops.size() + "已列示，应处置" + due + "单、已归档" + closed + "单，闭环率" + closeRate + "%。");
        r.setReporter("系统自动生成");
        r.setReportTime(LocalDateTime.now());
        r.setStatus("草稿");
        r.setTotalCount(total);
        r.setNegativeCount(negative);
        r.setTop10(joiner.length() == 0 ? null : joiner.toString());
        r.setClosedCount(closed);
        r.setDueCount(due);
        r.setCloseRate(closeRate);
        r.setMomChange(mom);
        r.setDataNote(dataNote);
        r.setTemplateId(templateId);
        r.setCreateTime(LocalDateTime.now());
        reportMapper.insert(r);
        auditService.log("舆情报告", "自动生成", r.getId(), title);
        return r;
    }

    @Transactional
    public OpinionReport save(OpinionReport report) {
        if (report.getTitle() == null || report.getTitle().isBlank()) {
            throw PartyBizException.block("E-OP-18", "报告标题必填");
        }
        if (report.getId() == null) {
            if (report.getReportNo() == null || report.getReportNo().isBlank()) {
                report.setReportNo("OP-M" + System.currentTimeMillis());
            }
            report.setReporter(CurrentHeader.operator());
            report.setCreateTime(LocalDateTime.now());
            reportMapper.insert(report);
        } else {
            report.setUpdateTime(LocalDateTime.now());
            reportMapper.updateById(report);
        }
        return report;
    }

    @Transactional
    public OpinionReport publish(Long id) {
        OpinionReport r = detail(id);
        r.setStatus("已发布");
        r.setUpdateTime(LocalDateTime.now());
        reportMapper.updateById(r);
        auditService.log("舆情报告", "发布", id, r.getTitle());
        return r;
    }

    @Transactional
    public void delete(Long id) {
        reportMapper.deleteById(id);
    }
}
