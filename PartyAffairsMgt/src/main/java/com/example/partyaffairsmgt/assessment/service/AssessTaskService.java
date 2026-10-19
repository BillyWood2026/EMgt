package com.example.partyaffairsmgt.assessment.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.activity.entity.ThreeMeetingsLesson;
import com.example.partyaffairsmgt.activity.mapper.ThreeMeetingsLessonMapper;
import com.example.partyaffairsmgt.assessment.entity.AssessAppeal;
import com.example.partyaffairsmgt.assessment.entity.AssessCheck;
import com.example.partyaffairsmgt.assessment.entity.AssessIndicator;
import com.example.partyaffairsmgt.assessment.entity.AssessScheme;
import com.example.partyaffairsmgt.assessment.entity.AssessScore;
import com.example.partyaffairsmgt.assessment.entity.AssessSelfEval;
import com.example.partyaffairsmgt.assessment.entity.AssessTask;
import com.example.partyaffairsmgt.assessment.mapper.AssessAppealMapper;
import com.example.partyaffairsmgt.assessment.mapper.AssessCheckMapper;
import com.example.partyaffairsmgt.assessment.mapper.AssessIndicatorMapper;
import com.example.partyaffairsmgt.assessment.mapper.AssessSchemeMapper;
import com.example.partyaffairsmgt.assessment.mapper.AssessScoreMapper;
import com.example.partyaffairsmgt.assessment.mapper.AssessSelfEvalMapper;
import com.example.partyaffairsmgt.assessment.mapper.AssessTaskMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 考核流程与评分算法服务（需求 10.3/10.4）。
 * 流程：待自评→待核查→待评分→待审定→申诉期→已发布→整改中→已闭环。
 * 算法：定量按完成率线性映射；定性去一个最高分一个最低分取均值；
 * 总分=Σ(得分×权重)+加减分（封顶±cap，超限截断并提示 E-AS-12）；
 * 一票否决项核查命中直接定为不合格（E-AS-08）；
 * 等级按分数线判定，发布时优秀名额按比例限制。
 * 错误码：E-AS-05 自评未提交不能核查；E-AS-06 核查意见为空阻断评分；
 * E-AS-07 申诉超期未处理；E-AS-08 一票否决；E-AS-09 未审定不得发布；
 * E-AS-10 申诉期未结束不得发布；E-AS-11 流程状态非法；E-AS-12 加减分超封顶。
 */
@Service
public class AssessTaskService {

    @Autowired
    private AssessTaskMapper taskMapper;
    @Autowired
    private AssessSchemeMapper schemeMapper;
    @Autowired
    private AssessIndicatorMapper indicatorMapper;
    @Autowired
    private AssessSelfEvalMapper selfEvalMapper;
    @Autowired
    private AssessCheckMapper checkMapper;
    @Autowired
    private AssessScoreMapper scoreMapper;
    @Autowired
    private AssessAppealMapper appealMapper;
    @Autowired
    private ThreeMeetingsLessonMapper ledgerMapper;
    @Autowired
    private AuditService auditService;

    public List<AssessTask> list(Long schemeId, String status, Long orgId) {
        QueryWrapper<AssessTask> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (schemeId != null) qw.eq("scheme_id", schemeId);
        if (status != null && !status.isBlank()) qw.eq("status", status);
        if (orgId != null) qw.eq("org_id", orgId);
        qw.orderByAsc("id");
        return taskMapper.selectList(qw);
    }

    public AssessTask requireTask(Long id) {
        AssessTask t = taskMapper.selectById(id);
        if (t == null || t.getDeleted() == 1) throw PartyBizException.block("E-AS-11", "考核任务不存在");
        return t;
    }

    private void requireStatus(AssessTask t, String expect, String action) {
        if (!expect.equals(t.getStatus())) {
            throw PartyBizException.block("E-AS-11",
                    "当前状态[" + t.getStatus() + "]不允许" + action + "（需要状态：" + expect + "）");
        }
    }

    // ============ 10.3 自评 ============

    /** 自评填报（逐项 upsert）。 */
    @Transactional
    public AssessSelfEval saveSelfEval(Long taskId, AssessSelfEval e) {
        AssessTask t = requireTask(taskId);
        if (!"待自评".equals(t.getStatus())) {
            throw PartyBizException.block("E-AS-11", "当前状态[" + t.getStatus() + "]不允许填报自评");
        }
        e.setTaskId(taskId);
        AssessSelfEval exist = selfEvalMapper.selectOne(new QueryWrapper<AssessSelfEval>()
                .eq("deleted", 0).eq("task_id", taskId).eq("indicator_id", e.getIndicatorId()).last("LIMIT 1"));
        if (exist != null) {
            e.setId(exist.getId());
            selfEvalMapper.updateById(e);
        } else {
            e.setCreateTime(LocalDateTime.now());
            selfEvalMapper.insert(e);
        }
        return e;
    }

    /** 提交自评：要求全部末级指标均已填报。 */
    @Transactional
    public AssessTask submitSelf(Long taskId, String submitBy) {
        AssessTask t = requireTask(taskId);
        requireStatus(t, "待自评", "提交自评");
        AssessScheme s = schemeMapper.selectById(t.getSchemeId());
        int need = snapshotIndicatorIds(s).size();
        Long filled = selfEvalMapper.selectCount(new QueryWrapper<AssessSelfEval>()
                .eq("deleted", 0).eq("task_id", taskId));
        if (filled < need) {
            throw PartyBizException.block("E-AS-11",
                    "自评未填完：已填" + filled + "/" + need + "项，不能提交");
        }
        t.setStatus("待核查");
        t.setSelfSubmitTime(LocalDateTime.now());
        t.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(t);
        auditService.log("党建考核", "提交自评", taskId, t.getOrgName() + (submitBy != null ? "/" + submitBy : ""));
        return t;
    }

    // ============ 10.3 核查（含业务数据自动取数） ============

    /**
     * 定量指标自动取数：按 data_source 从业务模块计算完成率%。
     * 三会一课/组织生活：查四本台账应开与已归档；其余来源返回 null 由核查人录入。
     */
    public Map<String, Object> autoFetch(Long taskId, Long indicatorId) {
        AssessTask t = requireTask(taskId);
        AssessIndicator ind = indicatorMapper.selectById(indicatorId);
        if (ind == null) throw PartyBizException.block("E-AS-11", "指标不存在");
        AssessScheme s = schemeMapper.selectById(t.getSchemeId());
        BigDecimal value = null;
        String note = null;
        if ("三会一课".equals(ind.getDataSource()) || "组织生活".equals(ind.getDataSource())) {
            int year = s.getAssessYear() == null ? LocalDate.now().getYear() : s.getAssessYear();
            List<ThreeMeetingsLesson> ledgers = ledgerMapper.selectList(new QueryWrapper<ThreeMeetingsLesson>()
                    .eq("deleted", 0).eq("org_id", t.getOrgId()).eq("period_year", year));
            int archived = (int) ledgers.stream().filter(l -> "已归档".equals(l.getStatus())).count();
            // 年度应召开：党员大会4+支委会12+党小组会12+党课4=32
            int should = 32;
            value = BigDecimal.valueOf(Math.min(archived, should) * 100.0 / should).setScale(2, RoundingMode.HALF_UP);
            note = "已归档" + archived + "/应召开" + should;
        } else {
            note = "数据来源[" + ind.getDataSource() + "]暂无自动取数接口，请人工核查录入";
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("indicatorId", indicatorId);
        r.put("dataSource", ind.getDataSource());
        r.put("dataValue", value);
        r.put("note", note);
        return r;
    }

    /** 核查明细录入（逐项 upsert，意见在 finishCheck 统一强校验）。 */
    @Transactional
    public AssessCheck saveCheck(Long taskId, AssessCheck c) {
        AssessTask t = requireTask(taskId);
        requireStatus(t, "待核查", "录入核查");
        c.setTaskId(taskId);
        c.setCheckTime(LocalDateTime.now());
        AssessCheck exist = checkMapper.selectOne(new QueryWrapper<AssessCheck>()
                .eq("deleted", 0).eq("task_id", taskId).eq("indicator_id", c.getIndicatorId()).last("LIMIT 1"));
        if (exist != null) {
            c.setId(exist.getId());
            checkMapper.updateById(c);
        } else {
            checkMapper.insert(c);
        }
        return c;
    }

    /** 完成核查：E-AS-05 自评未提交阻断；E-AS-06 任一核查意见为空阻断进入评分。 */
    @Transactional
    public AssessTask finishCheck(Long taskId) {
        AssessTask t = requireTask(taskId);
        if (t.getSelfSubmitTime() == null) {
            throw PartyBizException.block("E-AS-05", "该党组织自评未提交，不能进入核查");
        }
        requireStatus(t, "待核查", "完成核查");
        List<AssessCheck> checks = checkMapper.selectList(new QueryWrapper<AssessCheck>()
                .eq("deleted", 0).eq("task_id", taskId));
        AssessScheme s = schemeMapper.selectById(t.getSchemeId());
        int need = snapshotIndicatorIds(s).size();
        if (checks.size() < need) {
            throw PartyBizException.block("E-AS-06",
                    "核查未完成：已核查" + checks.size() + "/" + need + "项");
        }
        for (AssessCheck c : checks) {
            if (c.getCheckOpinion() == null || c.getCheckOpinion().isBlank()) {
                AssessIndicator ind = indicatorMapper.selectById(c.getIndicatorId());
                throw PartyBizException.block("E-AS-06",
                        "指标[" + (ind != null ? ind.getIndicatorName() : c.getIndicatorId()) + "]核查意见为空，阻断进入评分");
            }
        }
        boolean veto = checks.stream().anyMatch(c -> c.getVetoHit() != null && c.getVetoHit() == 1);
        t.setVetoTriggered(veto ? 1 : 0);
        t.setStatus("待评分");
        t.setCheckTime(LocalDateTime.now());
        t.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(t);
        auditService.log("党建考核", "完成核查", taskId, t.getOrgName() + (veto ? "（触发一票否决）" : ""));
        return t;
    }

    // ============ 10.4 评分算法 ============

    /** 定性评分：多人打分去一个最高分一个最低分取均值（<3人直接均值）。 */
    public static BigDecimal trimMean(String judgeScores) {
        if (judgeScores == null || judgeScores.isBlank()) return null;
        List<BigDecimal> vs = new ArrayList<>();
        for (String p : judgeScores.split(",")) {
            String x = p.trim();
            if (!x.isEmpty()) vs.add(new BigDecimal(x));
        }
        if (vs.isEmpty()) return null;
        if (vs.size() >= 3) {
            vs.remove(vs.stream().max(Comparator.naturalOrder()).get());
            vs.remove(vs.stream().min(Comparator.naturalOrder()).get());
        }
        BigDecimal sum = vs.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(vs.size()), 2, RoundingMode.HALF_UP);
    }

    /** 综合评分：汇总各指标得分×权重 + 加减分（封顶截断 E-AS-12），判定等级，进入待审定。 */
    @Transactional
    public AssessTask finishScore(Long taskId, List<AssessScore> scores, BigDecimal adjustScore) {
        AssessTask t = requireTask(taskId);
        requireStatus(t, "待评分", "综合评分");
        AssessScheme s = schemeMapper.selectById(t.getSchemeId());

        // 清旧评分重算（deleted 带 @TableLogic，须走 deleteById）
        List<AssessScore> old = scoreMapper.selectList(new QueryWrapper<AssessScore>().eq("task_id", taskId));
        for (AssessScore o : old) { scoreMapper.deleteById(o.getId()); }

        BigDecimal total = BigDecimal.ZERO;
        for (AssessScore sc : scores) {
            AssessIndicator ind = indicatorMapper.selectById(sc.getIndicatorId());
            if (ind == null) continue;
            BigDecimal finalScore;
            if ("定性".equals(ind.getScoreMode())) {
                finalScore = trimMean(sc.getJudgeScores());
                if (finalScore == null) {
                    throw PartyBizException.block("E-AS-11", "定性指标[" + ind.getIndicatorName() + "]未录入评委分");
                }
            } else {
                finalScore = sc.getAutoScore() != null ? sc.getAutoScore() : sc.getFinalScore();
                if (finalScore == null) {
                    throw PartyBizException.block("E-AS-11", "指标[" + ind.getIndicatorName() + "]未评分");
                }
            }
            BigDecimal capScore = ind.getScore() == null ? BigDecimal.valueOf(100) : ind.getScore();
            if (finalScore.compareTo(capScore) > 0) finalScore = capScore;
            if (finalScore.compareTo(BigDecimal.ZERO) < 0) finalScore = BigDecimal.ZERO;
            sc.setTaskId(taskId);
            sc.setScoreMode(ind.getScoreMode());
            sc.setFinalScore(finalScore);
            sc.setCreateTime(LocalDateTime.now());
            scoreMapper.insert(sc);
            BigDecimal w = ind.getWeight() == null ? BigDecimal.ZERO : ind.getWeight();
            total = total.add(finalScore.multiply(w).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        }

        // 加减分封顶（E-AS-12）
        BigDecimal cap = s.getAdjustCap() == null ? BigDecimal.valueOf(5) : s.getAdjustCap();
        BigDecimal adjust = adjustScore == null ? BigDecimal.ZERO : adjustScore;
        boolean truncated = false;
        if (adjust.compareTo(cap) > 0) { adjust = cap; truncated = true; }
        if (adjust.compareTo(cap.negate()) < 0) { adjust = cap.negate(); truncated = true; }

        total = total.add(adjust).setScale(2, RoundingMode.HALF_UP);
        t.setTotalScore(total);
        t.setAdjustScore(adjust);

        // 等级判定：一票否决直接不合格（E-AS-08）
        if (t.getVetoTriggered() != null && t.getVetoTriggered() == 1) {
            t.setGrade("不合格");
        } else if (total.compareTo(s.getExcellentLine()) >= 0) {
            t.setGrade("优秀");
        } else if (total.compareTo(s.getGoodLine()) >= 0) {
            t.setGrade("良好");
        } else if (total.compareTo(s.getPassLine()) >= 0) {
            t.setGrade("合格");
        } else {
            t.setGrade("不合格");
        }
        t.setStatus("待审定");
        t.setScoreTime(LocalDateTime.now());
        t.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(t);
        String extra = truncated ? "（加减分超封顶±" + cap + "，已截断 E-AS-12）" : "";
        if (t.getVetoTriggered() != null && t.getVetoTriggered() == 1) extra += "（触发一票否决 E-AS-08）";
        auditService.log("党建考核", "综合评分", taskId, t.getOrgName() + " 总分" + total + " 等级" + t.getGrade() + extra);
        return t;
    }

    // ============ 10.3 审定 / 申诉 / 发布 ============

    /** 结果审定（党委会审议）：进入申诉期并设定申诉截止。 */
    @Transactional
    public AssessTask approve(Long taskId, String approver, String opinion) {
        AssessTask t = requireTask(taskId);
        requireStatus(t, "待审定", "审定");
        AssessScheme s = schemeMapper.selectById(t.getSchemeId());
        t.setStatus("申诉期");
        t.setApproveTime(LocalDateTime.now());
        t.setApprover(approver);
        t.setApproveOpinion(opinion);
        t.setAppealDeadline(LocalDate.now().plusDays(s.getAppealDays() == null ? 5 : s.getAppealDays()));
        t.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(t);
        auditService.log("党建考核", "结果审定", taskId, t.getOrgName() + "/" + approver);
        return t;
    }

    /** 申诉提交：仅限申诉期内。 */
    @Transactional
    public AssessAppeal submitAppeal(Long taskId, String reason) {
        AssessTask t = requireTask(taskId);
        requireStatus(t, "申诉期", "提交申诉");
        if (t.getAppealDeadline() != null && LocalDate.now().isAfter(t.getAppealDeadline())) {
            throw PartyBizException.block("E-AS-11", "申诉期已截止（" + t.getAppealDeadline() + "），不能提交申诉");
        }
        AssessAppeal a = new AssessAppeal();
        a.setTaskId(taskId);
        a.setReason(reason);
        a.setStatus("待处理");
        a.setScoreBefore(t.getTotalScore());
        a.setCreateTime(LocalDateTime.now());
        appealMapper.insert(a);
        auditService.log("党建考核", "提交申诉", a.getId(), t.getOrgName());
        return a;
    }

    /** 申诉复核：成立则改分并留痕（原分与复核分并存）。 */
    @Transactional
    public AssessAppeal handleAppeal(Long appealId, boolean upheld, BigDecimal newScore,
                                     String handleResult, String handleBy) {
        AssessAppeal a = appealMapper.selectById(appealId);
        if (a == null) throw PartyBizException.block("E-AS-11", "申诉不存在");
        if (!"待处理".equals(a.getStatus())) throw PartyBizException.block("E-AS-11", "申诉已处理，不能重复操作");
        AssessTask t = requireTask(a.getTaskId());
        a.setStatus(upheld ? "已复核" : "已驳回");
        a.setHandleResult(handleResult);
        a.setHandleBy(handleBy);
        a.setHandleTime(LocalDateTime.now());
        if (upheld) {
            if (newScore == null) throw PartyBizException.block("E-AS-11", "申诉成立须给出复核分数");
            a.setScoreAfter(newScore);
            t.setTotalScore(newScore);
            AssessScheme s = schemeMapper.selectById(t.getSchemeId());
            if (t.getVetoTriggered() != null && t.getVetoTriggered() == 1) {
                t.setGrade("不合格");
            } else if (newScore.compareTo(s.getExcellentLine()) >= 0) {
                t.setGrade("优秀");
            } else if (newScore.compareTo(s.getGoodLine()) >= 0) {
                t.setGrade("良好");
            } else if (newScore.compareTo(s.getPassLine()) >= 0) {
                t.setGrade("合格");
            } else {
                t.setGrade("不合格");
            }
            t.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(t);
        }
        appealMapper.updateById(a);
        auditService.log("党建考核", "申诉复核", appealId,
                t.getOrgName() + (upheld ? " 成立 " + a.getScoreBefore() + "→" + newScore : " 驳回"));
        return a;
    }

    /** 申诉超期未处理检查（E-AS-07 预警）。 */
    public List<Map<String, Object>> appealTimeoutCheck() {
        List<AssessAppeal> pending = appealMapper.selectList(new QueryWrapper<AssessAppeal>()
                .eq("deleted", 0).eq("status", "待处理"));
        List<Map<String, Object>> overdue = new ArrayList<>();
        for (AssessAppeal a : pending) {
            AssessTask t = taskMapper.selectById(a.getTaskId());
            if (t != null && t.getAppealDeadline() != null && LocalDate.now().isAfter(t.getAppealDeadline())) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("appealId", a.getId());
                m.put("taskId", t.getId());
                m.put("orgName", t.getOrgName());
                m.put("appealDeadline", t.getAppealDeadline());
                overdue.add(m);
            }
        }
        if (!overdue.isEmpty()) {
            throw new PartyBizException("E-AS-07", PartyBizException.WARN,
                    "存在" + overdue.size() + "条申诉超期未处理：" + overdue);
        }
        return overdue;
    }

    /**
     * 排名发布：E-AS-09 未审定阻断；E-AS-10 申诉期未结束或存在未处理申诉阻断；
     * 按总分降序排名；优秀名额按比例限制（超出降为良好）。
     */
    @Transactional
    public Map<String, Object> publishResults(Long schemeId, boolean force) {
        AssessScheme s = schemeMapper.selectById(schemeId);
        if (s == null) throw PartyBizException.block("E-AS-11", "方案不存在");
        List<AssessTask> tasks = taskMapper.selectList(new QueryWrapper<AssessTask>()
                .eq("deleted", 0).eq("scheme_id", schemeId));
        if (tasks.isEmpty()) throw PartyBizException.block("E-AS-11", "该方案下无考核任务");

        List<String> notApproved = new ArrayList<>();
        List<String> inAppeal = new ArrayList<>();
        for (AssessTask t : tasks) {
            if (!"申诉期".equals(t.getStatus()) && !"已发布".equals(t.getStatus())) {
                notApproved.add(t.getOrgName() + "(" + t.getStatus() + ")");
            } else if ("申诉期".equals(t.getStatus())) {
                Long pendingAppeal = appealMapper.selectCount(new QueryWrapper<AssessAppeal>()
                        .eq("deleted", 0).eq("task_id", t.getId()).eq("status", "待处理"));
                boolean deadlinePass = t.getAppealDeadline() != null && !LocalDate.now().isAfter(t.getAppealDeadline());
                if (pendingAppeal > 0 || deadlinePass) {
                    inAppeal.add(t.getOrgName());
                }
            }
        }
        if (!notApproved.isEmpty()) {
            throw PartyBizException.block("E-AS-09", "存在未审定结果，不得发布：" + String.join("、", notApproved));
        }
        if (!inAppeal.isEmpty() && !force) {
            throw PartyBizException.block("E-AS-10",
                    "以下组织申诉期未结束或存在未处理申诉，不得发布排名：" + String.join("、", inAppeal));
        }

        // 排名 + 优秀比例限制
        List<AssessTask> sorted = new ArrayList<>(tasks);
        sorted.sort((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()));
        int excellentMax = (int) Math.ceil(sorted.size() * (s.getExcellentRatio() == null ? 30 : s.getExcellentRatio()).doubleValue() / 100.0);
        int excellentCount = 0;
        int rank = 0;
        BigDecimal prev = null;
        for (AssessTask t : sorted) {
            if (prev == null || t.getTotalScore().compareTo(prev) != 0) rank++;
            prev = t.getTotalScore();
            t.setRankNo(rank);
            if ("优秀".equals(t.getGrade())) {
                excellentCount++;
                if (excellentCount > excellentMax) {
                    t.setGrade("良好");
                }
            }
            t.setStatus("已发布");
            t.setPublishTime(LocalDateTime.now());
            t.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(t);
        }
        auditService.log("党建考核", "发布排名", schemeId, s.getSchemeName() + "，共" + sorted.size() + "个组织");
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("published", sorted.size());
        r.put("excellentMax", excellentMax);
        r.put("tasks", sorted);
        return r;
    }

    /** 解析方案权重快照中的指标ID列表。 */
    private List<Long> snapshotIndicatorIds(AssessScheme s) {
        List<Long> ids = new ArrayList<>();
        if (s != null && s.getWeightSnapshot() != null) {
            for (String part : s.getWeightSnapshot().split("\"indicatorId\":")) {
                if (part.startsWith("[")) continue;
                int end = part.indexOf(',');
                if (end > 0) {
                    try { ids.add(Long.valueOf(part.substring(0, end).trim())); } catch (NumberFormatException ignored) {}
                }
            }
        }
        if (ids.isEmpty()) {
            // 快照缺失时回退为全部末级启用指标
            for (AssessIndicator i : indicatorMapper.selectList(new QueryWrapper<AssessIndicator>()
                    .eq("deleted", 0).eq("level", 2).eq("status", "启用"))) {
                ids.add(i.getId());
            }
        }
        return ids;
    }

    public List<AssessSelfEval> selfEvalList(Long taskId) {
        return selfEvalMapper.selectList(new QueryWrapper<AssessSelfEval>()
                .eq("deleted", 0).eq("task_id", taskId).orderByAsc("indicator_id"));
    }

    public List<AssessCheck> checkList(Long taskId) {
        return checkMapper.selectList(new QueryWrapper<AssessCheck>()
                .eq("deleted", 0).eq("task_id", taskId).orderByAsc("indicator_id"));
    }

    public List<AssessScore> scoreList(Long taskId) {
        return scoreMapper.selectList(new QueryWrapper<AssessScore>()
                .eq("deleted", 0).eq("task_id", taskId).orderByAsc("indicator_id"));
    }

    public List<AssessAppeal> appealList(Long taskId) {
        QueryWrapper<AssessAppeal> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (taskId != null) qw.eq("task_id", taskId);
        qw.orderByDesc("id");
        return appealMapper.selectList(qw);
    }
}
