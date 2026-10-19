package com.example.partyaffairsmgt.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.activity.entity.ActivityAssessment;
import com.example.partyaffairsmgt.activity.entity.ActivityRecord;
import com.example.partyaffairsmgt.activity.entity.ThreeMeetingsLesson;
import com.example.partyaffairsmgt.activity.mapper.ActivityAssessmentMapper;
import com.example.partyaffairsmgt.activity.mapper.ActivityRecordMapper;
import com.example.partyaffairsmgt.activity.mapper.ThreeMeetingsLessonMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 活动统计与考核服务（需求 9.5）。
 * 完成率=已归档次数/应召开×100%；参与率=参与人次/(应参与党员数×活动次数)×100%；
 * 完成率<100%或参与率<80%（参数化）判定未达标，连续未达标联动第10章；
 * 记录/签到缺失导致口径不全时标注"数据待补齐"，不生成虚假数据。
 * 错误码：E-AC-13 参与率未达标；E-AC-14 数据待补齐。
 */
@Service
public class ActivityAssessmentService {

    /** 参与率达标阈值（参数化） */
    public static final double PARTICIPATION_THRESHOLD = 80.0;

    @Autowired
    private ActivityAssessmentMapper assessmentMapper;
    @Autowired
    private ActivityRecordMapper recordMapper;
    @Autowired
    private ThreeMeetingsLessonMapper ledgerMapper;
    @Autowired
    private AuditService auditService;

    public List<ActivityAssessment> list(Long orgId, String periodType, Integer year) {
        QueryWrapper<ActivityAssessment> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (orgId != null) qw.eq("org_id", orgId);
        if (periodType != null && !periodType.isBlank()) qw.eq("period_type", periodType);
        if (year != null) qw.eq("assess_year", year);
        qw.orderByDesc("assess_date");
        return assessmentMapper.selectList(qw);
    }

    /**
     * 三会一课完成率看板（按支部、周期）。数据异常时标注"数据待补齐"（E-AC-14）。
     */
    public Map<String, Object> dashboard(Long orgId, Integer year, Integer quarter) {
        // 应召开次数（四本台账）
        int should = 0;
        for (int v : (quarter == null
                ? ThreeMeetingsLessonService.YEAR_REQUIRED.values()
                : ThreeMeetingsLessonService.QUARTER_REQUIRED.values())) {
            should += v;
        }
        QueryWrapper<ThreeMeetingsLesson> qw = new QueryWrapper<ThreeMeetingsLesson>()
                .eq("deleted", 0).eq("period_year", year)
                .eq(orgId != null, "org_id", orgId);
        if (quarter != null) qw.eq("period_quarter", quarter);
        List<ThreeMeetingsLesson> ledgers = ledgerMapper.selectList(qw);
        int actual = (int) ledgers.stream().filter(l -> "已归档".equals(l.getStatus())).count();
        BigDecimal qualifyRate = should == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(Math.min(actual, should) * 100.0 / should).setScale(2, RoundingMode.HALF_UP);

        // 参与率：已归档活动的签到人次 / (应参加人数之和)
        QueryWrapper<ActivityRecord> rq = new QueryWrapper<ActivityRecord>()
                .eq("deleted", 0).eq("status", "已归档");
        if (orgId != null) rq.eq("org_id", orgId);
        if (quarter != null) rq.apply("QUARTER(hold_date)={0}", quarter);
        List<ActivityRecord> records = recordMapper.selectList(rq);
        int expectSum = records.stream().mapToInt(x -> x.getExpectCount() == null ? 0 : x.getExpectCount()).sum();
        int signSum = records.stream().mapToInt(x -> x.getSignCount() == null ? 0 : x.getSignCount()).sum();
        BigDecimal participation = expectSum == 0 ? null
                : BigDecimal.valueOf(signSum * 100.0 / expectSum).setScale(2, RoundingMode.HALF_UP);

        // 数据口径完整性：已归档记录缺签到数 或 台账无记录 → 数据待补齐
        long recordsNoSign = records.stream().filter(x -> x.getSignCount() == null).count();
        String dataNote = null;
        if (records.isEmpty()) {
            dataNote = "周期内无已归档活动记录，统计口径不全，标注：数据待补齐";
        } else if (recordsNoSign > 0) {
            dataNote = recordsNoSign + "条活动记录缺失签到数据，参与率口径不全，标注：数据待补齐";
        }

        List<String> unqualified = new java.util.ArrayList<>();
        if (qualifyRate.compareTo(BigDecimal.valueOf(100)) < 0) {
            unqualified.add("完成率" + qualifyRate + "%<100%");
        }
        if (participation != null && participation.doubleValue() < PARTICIPATION_THRESHOLD) {
            unqualified.add("参与率" + participation + "%低于阈值" + (int) PARTICIPATION_THRESHOLD + "%（E-AC-13）");
        }

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("year", year);
        r.put("quarter", quarter);
        r.put("shouldCount", should);
        r.put("actualCount", actual);
        r.put("qualifyRate", qualifyRate);
        r.put("participationRate", participation);
        r.put("recordCount", records.size());
        r.put("expectSum", expectSum);
        r.put("signSum", signSum);
        r.put("unqualified", unqualified);
        r.put("dataNote", dataNote);
        return r;
    }

    /**
     * 生成考核（季度/年度）：聚合完成率与参与率，未达标自动生成扣分建议；数据缺失只标注不造假。
     */
    @Transactional
    public ActivityAssessment generate(Long orgId, String orgName, Integer year, Integer quarter,
                                       Integer memberTotal, boolean forceNoData) {
        Map<String, Object> dash = dashboard(orgId, year, quarter);
        String dataNote = (String) dash.get("dataNote");
        if (dataNote != null && !forceNoData) {
            throw new PartyBizException("E-AC-14", PartyBizException.WARN,
                    dataNote + "，请先补齐数据再生成考核（如确认按现有口径生成请勾选强制）");
        }
        @SuppressWarnings("unchecked")
        List<String> unq = (List<String>) dash.get("unqualified");

        ActivityAssessment a = new ActivityAssessment();
        a.setOrgId(orgId);
        a.setOrgName(orgName);
        a.setAssessYear(year);
        a.setPeriodType(quarter == null ? "年度" : "季度");
        a.setPeriodQuarter(quarter);
        a.setActualCount((Integer) dash.get("actualCount"));
        a.setPlanCount((Integer) dash.get("shouldCount"));
        a.setQualifyRate((BigDecimal) dash.get("qualifyRate"));
        a.setParticipationRate((BigDecimal) dash.get("participationRate"));
        a.setMemberTotal(memberTotal);
        a.setActivityTimes((Integer) dash.get("recordCount"));
        a.setAttendTimes((Integer) dash.get("signSum"));
        a.setDataNote(dataNote);
        a.setAssessDate(LocalDate.now());

        if (!unq.isEmpty()) {
            a.setUnqualifiedFlag(1);
            a.setDeductSuggestion("未达标项：" + String.join("；", unq)
                    + "。按规则联动第10章党建考核，建议扣5分并限期整改。");
            a.setScore(BigDecimal.valueOf(70));
            a.setGrade("基本合格");
            a.setComment("三会一课或党员参与率未达标，已生成扣分建议");
        } else {
            a.setUnqualifiedFlag(0);
            a.setScore(BigDecimal.valueOf(95));
            a.setGrade("优秀");
            a.setComment("三会一课制度落实到位，参与率达标");
        }
        a.setCreateTime(LocalDateTime.now());
        assessmentMapper.insert(a);
        auditService.log("三会一课", "生成考核", a.getId(),
                (quarter == null ? year + "年度" : year + "年第" + quarter + "季度") + "/" + orgName);
        return a;
    }

    /** 未达标清单 */
    public List<ActivityAssessment> unqualifiedList(Integer year) {
        return assessmentMapper.selectList(new QueryWrapper<ActivityAssessment>()
                .eq("deleted", 0).eq("unqualified_flag", 1)
                .eq(year != null, "assess_year", year).orderByDesc("assess_date"));
    }

    @Transactional
    public ActivityAssessment save(ActivityAssessment a) {
        a.setUpdateTime(LocalDateTime.now());
        if (a.getId() == null) {
            a.setCreateTime(LocalDateTime.now());
            assessmentMapper.insert(a);
        } else {
            assessmentMapper.updateById(a);
        }
        return a;
    }
}
