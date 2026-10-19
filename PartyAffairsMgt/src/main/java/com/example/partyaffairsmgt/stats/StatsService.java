package com.example.partyaffairsmgt.stats;

import com.example.partyaffairsmgt.common.party.PartyBizException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 统计分析中心（需求 11）：驾驶舱聚合、7张固定报表、下钻与导出。
 * 错误码：E-ST-01 报表/下钻类型不存在。
 */
@Service
public class StatsService {

    /** 大数量导出阈值，超过转异步任务 */
    public static final int EXPORT_ASYNC_THRESHOLD = 5000;

    /** 敏感字段（导出/下钻强制脱敏） */
    public static final List<String> SENSITIVE_KEYS = Arrays.asList(
            "idCard", "id_card", "phone", "contactPhone", "contact_phone", "cardNo", "card_no");

    @Autowired
    private JdbcTemplate jdbc;

    private List<Map<String, Object>> q(String sql, Object... args) {
        return jdbc.queryForList(sql, args);
    }

    private Object scalar(String sql, Object... args) {
        List<Map<String, Object>> r = q(sql, args);
        return r.isEmpty() ? null : r.get(0).values().iterator().next();
    }

    // ===================== 11.1 驾驶舱 =====================

    /** 核心指标卡：党组织数/党员数/三会一课完成率/党费收缴率/发展党员进度/舆情态势/考核均分。 */
    public Map<String, Object> overview() {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("orgCount", n("SELECT COUNT(*) FROM party_org WHERE deleted=0 AND status='正常'"));
        r.put("memberCount", n("SELECT COUNT(*) FROM party_member WHERE deleted=0"));
        r.put("fullMemberCount", n("SELECT COUNT(*) FROM party_member WHERE deleted=0 AND political_status='正式党员'"));
        r.put("probationCount", n("SELECT COUNT(*) FROM party_member WHERE deleted=0 AND political_status='预备党员'"));
        // 三会一课完成率：年度考核记录平均
        Object qualify = scalar("SELECT ROUND(AVG(qualify_rate),1) FROM activity_assessment WHERE deleted=0");
        r.put("meetingRate", qualify == null ? 100.0 : qualify);
        // 党费收缴率：实缴/应缴
        Object dueRate = scalar("SELECT ROUND(IFNULL(SUM(paid_amount),0)/NULLIF(SUM(due_amount),0)*100,1) FROM dues_payment WHERE deleted=0");
        r.put("duesRate", dueRate == null ? 100.0 : dueRate);
        // 发展党员进度：已转正/计划数
        r.put("devTotal", n("SELECT COUNT(*) FROM member_development WHERE deleted=0"));
        r.put("devFull", n("SELECT COUNT(*) FROM member_development WHERE deleted=0 AND stage='已转正'"));
        // 舆情态势
        r.put("opinionTotal", n("SELECT COUNT(*) FROM opinion_collection WHERE deleted=0"));
        r.put("opinionNegative", n("SELECT COUNT(*) FROM opinion_collection WHERE deleted=0 AND sentiment_hint='负面'"));
        r.put("opinionAlertOpen", n("SELECT COUNT(*) FROM opinion_alert WHERE deleted=0 AND status IN ('待处置','处置中')"));
        // 考核
        Object avg = scalar("SELECT ROUND(AVG(total_score),1) FROM assess_task WHERE deleted=0 AND total_score IS NOT NULL");
        r.put("assessAvg", avg == null ? 0 : avg);
        r.put("assessExcellent", n("SELECT COUNT(*) FROM assess_task WHERE deleted=0 AND grade='优秀'"));
        r.put("assessFail", n("SELECT COUNT(*) FROM assess_task WHERE deleted=0 AND grade='不合格'"));
        return r;
    }

    /** 近12个月活动趋势（举办场次+签到人次）。 */
    public List<Map<String, Object>> trend() {
        return q("SELECT DATE_FORMAT(hold_date,'%Y-%m') ym, COUNT(*) activityCount, IFNULL(SUM(sign_count),0) signCount "
                + "FROM activity_record WHERE deleted=0 AND hold_date >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) "
                + "GROUP BY ym ORDER BY ym");
    }

    /** 排行TOP榜：支部完成率 + 党员积分。 */
    public Map<String, Object> rank() {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("branchRank", q("SELECT org_name orgName, ROUND(AVG(qualify_rate),1) qualifyRate, ROUND(AVG(participation_rate),1) participationRate "
                + "FROM activity_assessment WHERE deleted=0 GROUP BY org_id, org_name ORDER BY qualifyRate DESC LIMIT 10"));
        r.put("memberScoreRank", q("SELECT member_name memberName, org_name orgName, SUM(score) totalScore "
                + "FROM score_flow WHERE deleted=0 AND audit_status='已批准' GROUP BY member_id, member_name, org_name ORDER BY totalScore DESC LIMIT 10"));
        r.put("assessRank", q("SELECT org_name orgName, total_score totalScore, grade, rank_no rankNo "
                + "FROM assess_task WHERE deleted=0 AND total_score IS NOT NULL ORDER BY total_score DESC LIMIT 10"));
        return r;
    }

    /** 组织分布：按组织类型统计组织数与党员数。 */
    public List<Map<String, Object>> orgDistribution() {
        return q("SELECT o.org_type orgType, COUNT(DISTINCT o.id) orgCount, "
                + "(SELECT COUNT(*) FROM party_member m WHERE m.deleted=0 AND m.org_id IN "
                + "  (SELECT id FROM party_org o2 WHERE o2.deleted=0 AND o2.org_type=o.org_type)) memberCount "
                + "FROM party_org o WHERE o.deleted=0 GROUP BY o.org_type ORDER BY orgCount DESC");
    }

    // ===================== 11.2 固定报表 =====================

    public static final Map<String, String> REPORT_NAMES = new LinkedHashMap<>();
    static {
        REPORT_NAMES.put("member", "党员统计报表");
        REPORT_NAMES.put("dues", "党费统计报表");
        REPORT_NAMES.put("activity", "组织生活统计报表");
        REPORT_NAMES.put("development", "发展党员统计报表");
        REPORT_NAMES.put("news", "新闻宣传统计报表");
        REPORT_NAMES.put("opinion", "舆情统计报表");
        REPORT_NAMES.put("assessment", "考核结果报表");
    }

    public List<Map<String, Object>> report(String key) {
        switch (key) {
            case "member":
                return q("SELECT org_name orgName, COUNT(*) total, "
                        + "SUM(political_status='正式党员') formal, SUM(political_status='预备党员') probation, "
                        + "SUM(float_status='流动') floating, SUM(float_status='失联') lost, "
                        + "ROUND(AVG(TIMESTAMPDIFF(YEAR, birth_date, CURDATE())),1) avgAge, "
                        + "SUM(education IN ('本科','硕士研究生','博士研究生')) bachelorPlus "
                        + "FROM party_member WHERE deleted=0 GROUP BY org_id, org_name ORDER BY total DESC");
            case "dues":
                return q("SELECT CONCAT(fee_year,'-',LPAD(fee_month,2,'0')) period, org_name orgName, "
                        + "ROUND(SUM(due_amount),2) dueAmount, ROUND(SUM(paid_amount),2) paidAmount, "
                        + "ROUND(IFNULL(SUM(paid_amount),0)/NULLIF(SUM(due_amount),0)*100,1) collectRate, "
                        + "SUM(backpay_id IS NOT NULL) backpayCnt, SUM(reduce_id IS NOT NULL) reduceCnt "
                        + "FROM dues_payment WHERE deleted=0 GROUP BY fee_year, fee_month, org_id, org_name "
                        + "ORDER BY fee_year DESC, fee_month DESC, orgName");
            case "activity":
                return q("SELECT org_name orgName, assess_year assessYear, period_type periodType, IFNULL(period_quarter,'-') quarter, "
                        + "plan_count planCount, actual_count actualCount, qualify_rate qualifyRate, participation_rate participationRate, "
                        + "IF(unqualified_flag=1,'未达标','达标') result, IFNULL(data_note,'') dataNote "
                        + "FROM activity_assessment WHERE deleted=0 ORDER BY assess_year DESC, period_quarter DESC, orgName");
            case "development":
                return q("SELECT stage, COUNT(*) cnt, COUNT(DISTINCT branch_id) branchCnt "
                        + "FROM member_development WHERE deleted=0 GROUP BY stage "
                        + "ORDER BY FIELD(stage,'申请入党','积极分子','发展对象','预备党员','已转正')");
            case "news":
                return q("SELECT c.name columnName, COUNT(a.id) publishCnt, IFNULL(SUM(a.view_count),0) viewCnt, "
                        + "IFNULL(SUM(a.like_count),0) likeCnt, "
                        + "ROUND(AVG(TIMESTAMPDIFF(HOUR,a.submit_time,a.publish_time)),1) avgReviewHours "
                        + "FROM news_article a LEFT JOIN news_column c ON a.column_id=c.id "
                        + "WHERE a.deleted=0 AND a.status='已发布' GROUP BY a.column_id, c.name ORDER BY publishCnt DESC");
            case "opinion":
                return q("SELECT IFNULL(sentiment_hint,'未分析') sentiment, COUNT(*) cnt, "
                        + "SUM(status='已预警') alerted, SUM(merged=1) merged "
                        + "FROM opinion_collection WHERE deleted=0 GROUP BY sentiment_hint");
            case "assessment":
                return q("SELECT s.scheme_name schemeName, t.org_name orgName, t.total_score totalScore, "
                        + "t.adjust_score adjustScore, t.grade, t.rank_no rankNo, t.status, "
                        + "(SELECT CONCAT(SUM(r2.status='已销号'),'/',COUNT(*)) FROM assess_rectify r2 WHERE r2.deleted=0 AND r2.task_id=t.id) rectifyClose "
                        + "FROM assess_task t LEFT JOIN assess_scheme s ON t.scheme_id=s.id "
                        + "WHERE t.deleted=0 ORDER BY s.id, t.rank_no");
            default:
                throw PartyBizException.block("E-ST-01", "报表不存在：" + key);
        }
    }

    // ===================== 11.3 下钻 =====================

    /** 逐级下钻至组织/党员/单据明细。type: member/activity/dues/news/opinion/assess/rectify */
    public List<Map<String, Object>> drill(String type, Long orgId, Long refId, String keyword) {
        String kw = (keyword == null || keyword.isBlank()) ? null : "%" + keyword.trim() + "%";
        switch (type) {
            case "member": {
                StringBuilder sql = new StringBuilder("SELECT id, member_no memberNo, name, gender, birth_date birthDate, "
                        + "id_card idCard, education, phone, org_name orgName, political_status politicalStatus, float_status floatStatus, join_date joinDate "
                        + "FROM party_member WHERE deleted=0");
                List<Object> args = new ArrayList<>();
                if (orgId != null) { sql.append(" AND org_id=?"); args.add(orgId); }
                if (kw != null) { sql.append(" AND (name LIKE ? OR member_no LIKE ?)"); args.add(kw); args.add(kw); }
                sql.append(" ORDER BY id LIMIT 500");
                return q(sql.toString(), args.toArray());
            }
            case "activity":
                return q("SELECT r.id, r.org_name orgName, r.activity_type activityType, r.title, r.hold_date holdDate, "
                        + "r.expect_count expectCount, r.sign_count signCount, r.attend_count attendCount, r.status "
                        + "FROM activity_record r "
                        + "WHERE r.deleted=0 " + (orgId != null ? "AND r.org_id=" + orgId + " " : "")
                        + "ORDER BY r.hold_date DESC LIMIT 500");
            case "dues":
                return q("SELECT id, org_name orgName, member_name memberName, CONCAT(fee_year,'-',LPAD(fee_month,2,'0')) period, "
                        + "due_amount dueAmount, paid_amount paidAmount, status, pay_method payMethod "
                        + "FROM dues_payment WHERE deleted=0 " + (orgId != null ? "AND org_id=" + orgId + " " : "")
                        + "ORDER BY fee_year DESC, fee_month DESC LIMIT 500");
            case "news":
                return q("SELECT a.id, a.title, c.name columnName, a.author, a.status, a.view_count viewCount, a.publish_time publishTime "
                        + "FROM news_article a LEFT JOIN news_column c ON a.column_id=c.id WHERE a.deleted=0 "
                        + (refId != null ? "AND a.column_id=" + refId + " " : "")
                        + (kw != null ? "AND a.title LIKE '" + kw.replace("%", "") + "%' " : "")
                        + "ORDER BY a.publish_time DESC LIMIT 500");
            case "opinion":
                return q("SELECT id, title, source_platform sourcePlatform, sentiment_hint sentiment, status, collect_time collectTime "
                        + "FROM opinion_collection WHERE deleted=0 "
                        + (kw != null ? "AND (title LIKE ? OR keywords LIKE ?) ".replace("?", "'" + kw.replace("%", "") + "%'") : "")
                        + "ORDER BY collect_time DESC LIMIT 500");
            case "assess":
                return q("SELECT t.id, s.scheme_name schemeName, t.org_name orgName, t.total_score totalScore, t.grade, t.rank_no rankNo, t.status "
                        + "FROM assess_task t LEFT JOIN assess_scheme s ON t.scheme_id=s.id WHERE t.deleted=0 "
                        + (refId != null ? "AND t.scheme_id=" + refId + " " : "")
                        + "ORDER BY t.total_score DESC LIMIT 500");
            case "rectify":
                return q("SELECT r.id, t.org_name orgName, r.problem, r.duty_person dutyPerson, r.deadline, r.status "
                        + "FROM assess_rectify r LEFT JOIN assess_task t ON r.task_id=t.id WHERE r.deleted=0 "
                        + (refId != null ? "AND r.task_id=" + refId + " " : "")
                        + "ORDER BY r.deadline LIMIT 500");
            default:
                throw PartyBizException.block("E-ST-01", "下钻类型不支持：" + type);
        }
    }

    // ===================== 导出脱敏 =====================

    /** 对行数据脱敏，返回实际脱敏的字段名。 */
    public static List<String> maskRow(Map<String, Object> row) {
        List<String> masked = new ArrayList<>();
        for (String k : SENSITIVE_KEYS) {
            if (row.containsKey(k) && row.get(k) != null) {
                String v = String.valueOf(row.get(k));
                if (v.length() >= 7) {
                    row.put(k, v.substring(0, 3) + "****" + v.substring(v.length() - 2));
                } else {
                    row.put(k, "***");
                }
                masked.add(k);
            }
        }
        return masked;
    }

    private long n(String sql, Object... args) {
        Object v = scalar(sql, args);
        return v == null ? 0 : ((Number) v).longValue();
    }
}
