package com.example.culturemgt.dashboard.service;

import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CurrentHeader;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.dashboard.mapper.DashboardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 子模块10 跨模块文化数据看板 - 聚合服务。
 * K1 集团总看板（C03/C07/C99）；K2 分组织看板（C02/C03/C07/C99，C02 限本组织）；
 * K3 明细钻取 + CSV 导出（confirm=true 二次确认，导出留痕审计）。
 */
@Service
public class DashboardService {

    /** 部门文化达标线（综合得分 ≥ 80 视为达标）。 */
    public static final double PASS_LINE = 80.0;

    private static final Pattern NUM = Pattern.compile("-?\\d+(\\.\\d+)?");

    @Autowired
    private DashboardMapper mapper;

    @Autowired
    private AuditService auditService;

    /** K2/K3 组织越权守卫：C02 仅限本组织（X-Org-Id 绑定），且必须显式传 org。 */
    public String guardOrg(String org) {
        boolean c02Only = CulRole.hasAny(CulRole.C02) && !CulRole.hasAny(CulRole.C03, CulRole.C99);
        String bound = CurrentHeader.get("X-Org-Id");
        if (bound != null && !bound.isBlank() && bound.contains("%")) {
            try {
                bound = java.net.URLDecoder.decode(bound, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception ignore) {
                /* 保持原值 */
            }
        }
        if (c02Only) {
            if (org == null || org.isBlank()) {
                if (bound == null || bound.isBlank()) {
                    throw DashboardFlow.block("04", "分组织看板需指定组织参数 org（E-DB-04）");
                }
                return bound.trim();
            }
            if (bound != null && !bound.isBlank() && !bound.trim().equals(org.trim())) {
                throw DashboardFlow.block("02", "越权组织访问：仅可查看本组织「" + bound.trim() + "」数据（E-DB-02）");
            }
        }
        return org == null || org.isBlank() ? null : org.trim();
    }

    /** K1/K2 看板聚合：org=null 全集团。 */
    public Map<String, Object> overview(String org) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("org", org == null ? "全集团" : org);
        result.put("genTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        /* 覆盖度 */
        Map<String, Object> study = nz(mapper.studyAgg(org));
        long studyTotal = num(study.get("total"));
        long studyDone = num(study.get("done"));
        Map<String, Object> exam = nz(mapper.examAgg(org));
        long examGraded = num(exam.get("graded"));
        long examPassed = num(exam.get("passed"));
        Map<String, Object> survey = nz(mapper.surveyAgg(org));
        long surveyTarget = num(survey.get("target"));
        long surveyResp = num(survey.get("responses"));
        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("study", Map.of("total", studyTotal, "done", studyDone, "rate", pct(studyDone, studyTotal)));
        kpi.put("exam", Map.of("graded", examGraded, "passed", examPassed, "rate", pct(examPassed, examGraded)));
        kpi.put("survey", Map.of("target", surveyTarget, "responses", surveyResp, "rate", pct(surveyResp, surveyTarget)));

        /* 活跃度 */
        Map<String, Object> news = nz(mapper.newsAgg(org));
        Map<String, Object> asset = nz(mapper.assetAgg(org));
        kpi.put("caseTotal", nzl(mapper.caseTotal(org)));
        kpi.put("activityJoin", nzl(mapper.activityJoin(org)));
        kpi.put("news", Map.of("published", num(news.get("published")), "reads", num(news.get("readTotal"))));
        kpi.put("asset", Map.of("views", num(asset.get("views")), "downloads", num(asset.get("downloads"))));

        /* 积分分布 + 低分人数 */
        Map<String, Object> scoreAgg = scoreAgg(org);
        kpi.put("score", scoreAgg.get("agg"));
        result.put("kpi", kpi);
        result.put("scoreDist", scoreAgg.get("dist"));

        /* 文化健康 */
        List<Map<String, Object>> deptScores = mapper.deptScores(org);
        result.put("deptScores", deptScores);
        Map<String, Object> deptPass = nz(mapper.deptPassAgg(org));
        long passTotal = num(deptPass.get("total"));
        long passNum = num(deptPass.get("pass"));
        result.put("deptPassRate", Map.of("total", passTotal, "pass", passNum,
                "rate", pct(passNum, passTotal), "passLine", PASS_LINE));
        result.put("climateIndex", climateIndex(org));

        /* 干部履职 */
        Map<String, Object> duty = nz(mapper.dutyAgg(org));
        long dutyTotal = num(duty.get("total"));
        long dutyDone = num(duty.get("submitted"));
        Map<String, Object> dutyOut = new LinkedHashMap<>();
        dutyOut.put("total", dutyTotal);
        dutyOut.put("submitted", dutyDone);
        dutyOut.put("rate", pct(dutyDone, dutyTotal));
        dutyOut.put("byDept", mapper.dutyByDept(org));
        result.put("duty", dutyOut);

        /* 五类智能预警 */
        Map<String, Object> alerts = new LinkedHashMap<>();
        alerts.put("studyOverdue", mapper.alertStudyOverdue(org));
        alerts.put("surveyLow", mapper.alertSurveyLow(org));
        alerts.put("lowScore", mapper.alertLowScore(org));
        alerts.put("rectifyOverdue", mapper.alertRectifyOverdue(org));
        alerts.put("newsPending", mapper.alertNewsPending(org));
        result.put("alerts", alerts);
        return result;
    }

    /** K3 明细钻取：type 非法报 E-DB-01。 */
    public List<Map<String, Object>> drill(String type, String org) {
        String t = type == null ? "" : type.trim().toLowerCase();
        return switch (t) {
            case "study" -> mapper.drillStudy(org);
            case "exam" -> mapper.drillExam(org);
            case "survey" -> mapper.drillSurvey(org);
            case "case" -> mapper.drillCase(org);
            case "activity" -> mapper.drillActivity(org);
            case "news" -> mapper.drillNews(org);
            case "asset" -> mapper.drillAsset(org);
            case "score" -> mapper.drillScore(org);
            case "duty" -> mapper.drillDuty(org);
            case "alert_study_overdue" -> mapper.alertStudyOverdue(org);
            case "alert_survey_low" -> mapper.alertSurveyLow(org);
            case "alert_low_score" -> mapper.alertLowScore(org);
            case "alert_rectify_overdue" -> mapper.alertRectifyOverdue(org);
            case "alert_news_pending" -> mapper.alertNewsPending(org);
            default -> throw DashboardFlow.block("01", "钻取类型非法：" + type
                    + "，可选 study/exam/survey/case/activity/news/asset/score/duty"
                    + "/alert_study_overdue/alert_survey_low/alert_low_score/alert_rectify_overdue/alert_news_pending"
                    + "（E-DB-01）");
        };
    }

    /** CSV 导出（confirm=true 才放行），并写审计留痕。 */
    public Map<String, Object> export(String type, String org, boolean confirm) {
        if (!confirm) {
            throw DashboardFlow.block("03", "导出为敏感操作，需二次确认 confirm=true（E-DB-03）");
        }
        List<Map<String, Object>> rows = drill(type, org);
        StringBuilder sb = new StringBuilder("\uFEFF");
        if (!rows.isEmpty()) {
            List<String> heads = new ArrayList<>(rows.get(0).keySet());
            sb.append(String.join(",", heads.stream().map(DashboardService::csvCell).toList())).append("\r\n");
            for (Map<String, Object> r : rows) {
                List<String> cells = new ArrayList<>();
                for (String h : heads) {
                    Object v = r.get(h);
                    cells.add(csvCell(v == null ? "" : v.toString()));
                }
                sb.append(String.join(",", cells)).append("\r\n");
            }
        }
        String fileName = "dashboard_" + type + "_" + LocalDate.now() + ".csv";
        auditService.log("EXPORT", "cul_dashboard", null,
                (org == null ? "全集团" : org) + " 导出 " + rows.size() + " 行 → " + fileName);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("fileName", fileName);
        out.put("rows", rows.size());
        out.put("csv", sb.toString());
        return out;
    }

    /* ---------- 私有辅助 ---------- */

    /** 积分分布 + 低分聚合。 */
    private Map<String, Object> scoreAgg(String org) {
        List<Map<String, Object>> dist = mapper.scoreDist(org);
        long total = 0;
        long low = 0;
        for (Map<String, Object> d : dist) {
            long n = num(d.get("num"));
            total += n;
            if ("低分".equals(d.get("bucket"))) {
                low = n;
            }
        }
        Map<String, Object> agg = new LinkedHashMap<>();
        agg.put("total", total);
        agg.put("low", low);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("agg", agg);
        out.put("dist", dist);
        return out;
    }

    /** 组织氛围指数：全部问卷维度分均值（dimension_scores 为 JSON 对象）。 */
    private double climateIndex(String org) {
        List<Map<String, Object>> rows = mapper.dimensionScores(org);
        double sum = 0;
        int cnt = 0;
        for (Map<String, Object> r : rows) {
            Object ds = r.get("ds");
            if (ds == null) {
                continue;
            }
            Matcher m = NUM.matcher(ds.toString());
            while (m.find()) {
                sum += Double.parseDouble(m.group());
                cnt++;
            }
        }
        return cnt == 0 ? 0.0 : Math.round(sum / cnt * 10.0) / 10.0;
    }

    private static double pct(long part, long total) {
        return total == 0 ? 0.0 : Math.round(part * 1000.0 / total) / 10.0;
    }

    private static long num(Object v) {
        return v instanceof Number n ? n.longValue() : 0L;
    }

    private static long nzl(Long v) {
        return v == null ? 0L : v;
    }

    private static Map<String, Object> nz(Map<String, Object> m) {
        return m == null ? new LinkedHashMap<>() : m;
    }

    private static String csvCell(String v) {
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            return '"' + v.replace("\"", "\"\"") + '"';
        }
        return v;
    }
}
