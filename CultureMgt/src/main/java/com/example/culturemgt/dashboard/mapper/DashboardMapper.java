package com.example.culturemgt.dashboard.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 子模块10 跨模块文化数据看板 - 只读聚合查询（跨子模块1-9数据源）。
 * org 参数为组织名（如 集团总部/华东子公司），null 表示全集团；
 * 部门字段（user_dept/dept/respondent_dept/org_name）按前缀匹配，news.source_org 按精确匹配。
 */
@Mapper
public interface DashboardMapper {

    /* ---------- 覆盖度 ---------- */

    @Select("<script>SELECT COUNT(*) AS total, COALESCE(SUM(progress &gt;= 100),0) AS done "
            + "FROM cul_study_record WHERE deleted = 0 "
            + "<if test='org != null'>AND user_dept LIKE CONCAT(#{org}, '%')</if></script>")
    Map<String, Object> studyAgg(@Param("org") String org);

    @Select("<script>SELECT COUNT(*) AS graded, COALESCE(SUM(status IN ('已完成合格','补考完成合格')),0) AS passed "
            + "FROM cul_exam_instance WHERE deleted = 0 AND status &lt;&gt; '待考试' "
            + "<if test='org != null'>AND user_dept LIKE CONCAT(#{org}, '%')</if></script>")
    Map<String, Object> examAgg(@Param("org") String org);

    @Select("<script>SELECT COALESCE(SUM(q.target_count),0) AS target, COUNT(r.id) AS responses "
            + "FROM cul_survey_questionnaire q LEFT JOIN cul_survey_response r "
            + "ON r.questionnaire_id = q.id AND r.deleted = 0 "
            + "WHERE q.deleted = 0 AND q.status IN ('已发布','已结束') AND q.target_count &gt; 0 "
            + "<if test='org != null'>AND q.target_org LIKE CONCAT('%', #{org}, '%')</if></script>")
    Map<String, Object> surveyAgg(@Param("org") String org);

    /* ---------- 活跃度 ---------- */

    @Select("<script>SELECT COUNT(*) AS num FROM cul_case WHERE deleted = 0 "
            + "<if test='org != null'>AND org_name LIKE CONCAT(#{org}, '%')</if></script>")
    Long caseTotal(@Param("org") String org);

    @Select("<script>SELECT COUNT(*) AS num FROM cul_activity_sign s "
            + "JOIN cul_activity a ON s.activity_id = a.id AND a.deleted = 0 "
            + "WHERE s.deleted = 0 "
            + "<if test='org != null'>AND s.user_dept LIKE CONCAT(#{org}, '%')</if></script>")
    Long activityJoin(@Param("org") String org);

    @Select("<script>SELECT COUNT(*) AS published, COALESCE(SUM(read_count + mobile_read_count),0) AS readTotal "
            + "FROM cul_news WHERE deleted = 0 AND status = '已发布' "
            + "<if test='org != null'>AND source_org = #{org}</if></script>")
    Map<String, Object> newsAgg(@Param("org") String org);

    @Select("<script>SELECT COALESCE(SUM(view_count),0) AS views, COALESCE(SUM(download_count),0) AS downloads "
            + "FROM cul_culture_asset WHERE deleted = 0 AND status = '启用' "
            + "<if test='org != null'>AND org_scope LIKE CONCAT('%', #{org}, '%')</if></script>")
    Map<String, Object> assetAgg(@Param("org") String org);

    /* ---------- 文化健康 ---------- */

    @Select("<script>SELECT CASE WHEN total_score &gt;= 90 THEN '优秀' WHEN total_score &gt;= 80 THEN '良好' "
            + "WHEN total_score &gt;= 60 THEN '合格' ELSE '低分' END AS bucket, COUNT(*) AS num "
            + "FROM cul_behavior_score_account WHERE deleted = 0 "
            + "<if test='org != null'>AND dept LIKE CONCAT(#{org}, '%')</if> "
            + "GROUP BY bucket</script>")
    List<Map<String, Object>> scoreDist(@Param("org") String org);

    @Select("<script>SELECT dept, ROUND(AVG(total_score),1) AS avgScore, COUNT(*) AS cnt "
            + "FROM cul_behavior_score_account WHERE deleted = 0 "
            + "<if test='org != null'>AND dept LIKE CONCAT(#{org}, '%')</if> "
            + "GROUP BY dept ORDER BY avgScore DESC</script>")
    List<Map<String, Object>> deptScores(@Param("org") String org);

    @Select("<script>SELECT dimension_scores AS ds FROM cul_survey_response WHERE deleted = 0 "
            + "<if test='org != null'>AND respondent_dept LIKE CONCAT(#{org}, '%')</if></script>")
    List<Map<String, Object>> dimensionScores(@Param("org") String org);

    @Select("<script>SELECT COUNT(*) AS total, COALESCE(SUM(total_score &gt;= 80),0) AS pass "
            + "FROM (SELECT dept, AVG(total_score) AS total_score FROM cul_behavior_score_account "
            + "WHERE deleted = 0 <if test='org != null'>AND dept LIKE CONCAT(#{org}, '%')</if> "
            + "GROUP BY dept) t</script>")
    Map<String, Object> deptPassAgg(@Param("org") String org);

    /* ---------- 干部履职 ---------- */

    @Select("<script>SELECT COUNT(*) AS total, COALESCE(SUM(status &lt;&gt; '草稿'),0) AS submitted "
            + "FROM cul_behavior_duty WHERE deleted = 0 "
            + "<if test='org != null'>AND dept LIKE CONCAT(#{org}, '%')</if></script>")
    Map<String, Object> dutyAgg(@Param("org") String org);

    @Select("<script>SELECT dept, COUNT(*) AS total, COALESCE(SUM(status &lt;&gt; '草稿'),0) AS submitted "
            + "FROM cul_behavior_duty WHERE deleted = 0 "
            + "<if test='org != null'>AND dept LIKE CONCAT(#{org}, '%')</if> "
            + "GROUP BY dept</script>")
    List<Map<String, Object>> dutyByDept(@Param("org") String org);

    /* ---------- 五类智能预警 ---------- */

    @Select("<script>SELECT r.user_dept AS dept, t.task_name AS taskName, COUNT(*) AS cnt "
            + "FROM cul_study_record r JOIN cul_study_task t ON r.task_id = t.id AND t.deleted = 0 "
            + "WHERE r.deleted = 0 AND r.status &lt;&gt; '已完成' AND t.deadline &lt; CURDATE() "
            + "<if test='org != null'>AND r.user_dept LIKE CONCAT(#{org}, '%')</if> "
            + "GROUP BY r.user_dept, t.task_name ORDER BY cnt DESC</script>")
    List<Map<String, Object>> alertStudyOverdue(@Param("org") String org);

    @Select("<script>SELECT q.title, q.target_count AS targetCount, COUNT(r.id) AS responses, "
            + "ROUND(COUNT(r.id) * 100.0 / q.target_count, 1) AS rate "
            + "FROM cul_survey_questionnaire q LEFT JOIN cul_survey_response r "
            + "ON r.questionnaire_id = q.id AND r.deleted = 0 "
            + "WHERE q.deleted = 0 AND q.status IN ('已发布','已结束') AND q.target_count &gt; 0 "
            + "<if test='org != null'>AND q.target_org LIKE CONCAT('%', #{org}, '%')</if> "
            + "GROUP BY q.id, q.title, q.target_count "
            + "HAVING COUNT(r.id) * 100.0 / q.target_count &lt; 60</script>")
    List<Map<String, Object>> alertSurveyLow(@Param("org") String org);

    @Select("<script>SELECT user_name AS userName, dept, total_score AS totalScore "
            + "FROM cul_behavior_score_account WHERE deleted = 0 AND total_score &lt; 60 "
            + "<if test='org != null'>AND dept LIKE CONCAT(#{org}, '%')</if> "
            + "ORDER BY total_score LIMIT 50</script>")
    List<Map<String, Object>> alertLowScore(@Param("org") String org);

    @Select("<script>SELECT title, dept, deadline, status FROM cul_survey_rectify "
            + "WHERE deleted = 0 AND status &lt;&gt; '验收闭环' AND deadline &lt; CURDATE() "
            + "<if test='org != null'>AND dept LIKE CONCAT(#{org}, '%')</if> "
            + "ORDER BY deadline</script>")
    List<Map<String, Object>> alertRectifyOverdue(@Param("org") String org);

    @Select("<script>SELECT title, source_org AS sourceOrg, submit_time AS submitTime, "
            + "TIMESTAMPDIFF(HOUR, submit_time, NOW()) AS waitHours FROM cul_news "
            + "WHERE deleted = 0 AND status = '待审核' AND submit_time IS NOT NULL "
            + "AND submit_time &lt; NOW() - INTERVAL 72 HOUR "
            + "<if test='org != null'>AND source_org = #{org}</if> "
            + "ORDER BY submit_time</script>")
    List<Map<String, Object>> alertNewsPending(@Param("org") String org);

    /* ---------- K3 明细钻取 ---------- */

    @Select("<script>SELECT task_name AS col1, user_name AS col2, user_dept AS col3, "
            + "progress AS col4, status AS col5, finish_time AS col6 "
            + "FROM cul_study_record WHERE deleted = 0 "
            + "<if test='org != null'>AND user_dept LIKE CONCAT(#{org}, '%')</if> "
            + "ORDER BY id DESC LIMIT 500</script>")
    List<Map<String, Object>> drillStudy(@Param("org") String org);

    @Select("<script>SELECT exam_name AS col1, user_name AS col2, user_dept AS col3, "
            + "status AS col4, score AS col5, attempt AS col6 "
            + "FROM cul_exam_instance WHERE deleted = 0 "
            + "<if test='org != null'>AND user_dept LIKE CONCAT(#{org}, '%')</if> "
            + "ORDER BY id DESC LIMIT 500</script>")
    List<Map<String, Object>> drillExam(@Param("org") String org);

    @Select("<script>SELECT title AS col1, respondent AS col2, respondent_dept AS col3, "
            + "submit_time AS col4 FROM cul_survey_response WHERE deleted = 0 "
            + "<if test='org != null'>AND respondent_dept LIKE CONCAT(#{org}, '%')</if> "
            + "ORDER BY id DESC LIMIT 500</script>")
    List<Map<String, Object>> drillSurvey(@Param("org") String org);

    @Select("<script>SELECT case_title AS col1, contributor AS col2, org_name AS col3, "
            + "status AS col4, point_total AS col5, publish_time AS col6 "
            + "FROM cul_case WHERE deleted = 0 "
            + "<if test='org != null'>AND org_name LIKE CONCAT(#{org}, '%')</if> "
            + "ORDER BY id DESC LIMIT 500</script>")
    List<Map<String, Object>> drillCase(@Param("org") String org);

    @Select("<script>SELECT activity_id AS col1, user_name AS col2, user_dept AS col3, "
            + "sign_status AS col4, check_status AS col5, sign_time AS col6 "
            + "FROM cul_activity_sign WHERE deleted = 0 "
            + "<if test='org != null'>AND user_dept LIKE CONCAT(#{org}, '%')</if> "
            + "ORDER BY id DESC LIMIT 500</script>")
    List<Map<String, Object>> drillActivity(@Param("org") String org);

    @Select("<script>SELECT title AS col1, source_org AS col2, status AS col3, "
            + "(read_count + mobile_read_count) AS col4, publish_time AS col5 "
            + "FROM cul_news WHERE deleted = 0 "
            + "<if test='org != null'>AND source_org = #{org}</if> "
            + "ORDER BY id DESC LIMIT 500</script>")
    List<Map<String, Object>> drillNews(@Param("org") String org);

    @Select("<script>SELECT asset_name AS col1, asset_type AS col2, view_count AS col3, "
            + "download_count AS col4, status AS col5 "
            + "FROM cul_culture_asset WHERE deleted = 0 "
            + "<if test='org != null'>AND org_scope LIKE CONCAT('%', #{org}, '%')</if> "
            + "ORDER BY id DESC LIMIT 500</script>")
    List<Map<String, Object>> drillAsset(@Param("org") String org);

    @Select("<script>SELECT user_name AS col1, dept AS col2, total_score AS col3, "
            + "pos_count AS col4, neg_count AS col5, last_calc_time AS col6 "
            + "FROM cul_behavior_score_account WHERE deleted = 0 "
            + "<if test='org != null'>AND dept LIKE CONCAT(#{org}, '%')</if> "
            + "ORDER BY total_score DESC LIMIT 500</script>")
    List<Map<String, Object>> drillScore(@Param("org") String org);

    @Select("<script>SELECT period_tag AS col1, officer AS col2, dept AS col3, status AS col4 "
            + "FROM cul_behavior_duty WHERE deleted = 0 "
            + "<if test='org != null'>AND dept LIKE CONCAT(#{org}, '%')</if> "
            + "ORDER BY id DESC LIMIT 500</script>")
    List<Map<String, Object>> drillDuty(@Param("org") String org);
}
