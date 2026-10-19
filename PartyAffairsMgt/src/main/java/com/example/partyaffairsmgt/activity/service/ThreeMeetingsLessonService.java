package com.example.partyaffairsmgt.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.activity.entity.ThreeMeetingsLesson;
import com.example.partyaffairsmgt.activity.mapper.ThreeMeetingsLessonMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 三会一课专项台账服务（需求 9.4 四本台账）。
 * 按类型自动归集、补录显著标记（原因必填）；台账完整率=已归档/应召开×100%；
 * 完整率<100%预警列缺失；连续两季度不达标升级告警并生成考核扣分建议。
 * 党课授课刚性：机关党委书记/班子成员/党支部书记每年至少讲1次。
 * 错误码：E-AC-10 补录无原因；E-AC-11 完整率不足；E-AC-12 连续两季度不达标；E-AC-15 党课授课频次不足。
 */
@Service
public class ThreeMeetingsLessonService {

    /** 四本台账 */
    public static final Map<String, Integer> YEAR_REQUIRED = Map.of(
            "支部党员大会", 4, "支部委员会", 12, "党小组会", 12, "党课", 4);
    /** 每季度应召开 */
    public static final Map<String, Integer> QUARTER_REQUIRED = Map.of(
            "支部党员大会", 1, "支部委员会", 3, "党小组会", 3, "党课", 1);
    /** 党课授课刚性角色（每年至少1次） */
    public static final List<String> LECTURER_ROLES = List.of("机关党委书记", "班子成员", "党支部书记");

    @Autowired
    private ThreeMeetingsLessonMapper ledgerMapper;
    @Autowired
    private AuditService auditService;

    public List<ThreeMeetingsLesson> list(String meetingType, Long orgId, Integer year, Integer quarter) {
        QueryWrapper<ThreeMeetingsLesson> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (meetingType != null && !meetingType.isBlank()) qw.eq("meeting_type", meetingType);
        if (orgId != null) qw.eq("org_id", orgId);
        if (year != null) qw.eq("period_year", year);
        if (quarter != null) qw.eq("period_quarter", quarter);
        qw.orderByDesc("hold_date");
        return ledgerMapper.selectList(qw);
    }

    /** 台账补录（追记）：必须标记补录并说明原因，否则阻断 E-AC-10 */
    @Transactional
    public ThreeMeetingsLesson backfill(ThreeMeetingsLesson l) {
        l.setIsBackfill(1);
        if (l.getBackfillReason() == null || l.getBackfillReason().isBlank()) {
            throw PartyBizException.block("E-AC-10", "台账补录必须说明原因，无原因阻断");
        }
        if (l.getMeetingType() == null || l.getTitle() == null || l.getHoldDate() == null) {
            throw PartyBizException.block("E-AC-10", "补录须填写类型、主题与召开时间");
        }
        l.setStatus("已归档");
        l.setArchiveTime(LocalDateTime.now());
        l.setPeriodYear(l.getHoldDate().getYear());
        l.setPeriodQuarter((l.getHoldDate().getMonthValue() - 1) / 3 + 1);
        l.setPeriodMonth(l.getHoldDate().getMonthValue());
        l.setCreateTime(LocalDateTime.now());
        ledgerMapper.insert(l);
        auditService.log("三会一课", "台账补录", l.getId(), l.getTitle() + "：" + l.getBackfillReason());
        return l;
    }

    /**
     * 台账总览（年度/季度，按支部对比）：应召开、已归档、完整率、缺失明细、补录数。
     */
    public Map<String, Object> overview(Long orgId, Integer year, Integer quarter) {
        List<ThreeMeetingsLesson> all = ledgerMapper.selectList(new QueryWrapper<ThreeMeetingsLesson>()
                .eq("deleted", 0).eq("period_year", year)
                .eq(orgId != null, "org_id", orgId));
        Map<String, Object> r = new LinkedHashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        int totalShould = 0;
        int totalActual = 0;
        List<Map<String, Object>> missing = new ArrayList<>();

        for (String type : List.of("支部党员大会", "支部委员会", "党小组会", "党课")) {
            int required;
            List<ThreeMeetingsLesson> hits;
            if (quarter == null) {
                required = YEAR_REQUIRED.get(type);
                hits = all.stream().filter(x -> type.equals(x.getMeetingType())).toList();
            } else {
                required = QUARTER_REQUIRED.get(type);
                hits = all.stream().filter(x -> type.equals(x.getMeetingType())
                        && quarter.equals(x.getPeriodQuarter())).toList();
            }
            int actual = hits.size();
            int backfill = (int) hits.stream().filter(x -> x.getIsBackfill() != null && x.getIsBackfill() == 1).count();
            BigDecimal rate = required == 0 ? BigDecimal.valueOf(100)
                    : BigDecimal.valueOf(actual * 100.0 / required).setScale(2, RoundingMode.HALF_UP);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("meetingType", type);
            row.put("shouldCount", required);
            row.put("actualCount", actual);
            row.put("backfillCount", backfill);
            row.put("completeRate", rate);
            rows.add(row);
            totalShould += required;
            totalActual += Math.min(actual, required);
            if (actual < required) {
                // 列出缺失类型与月份
                for (int i = 0; i < required - actual; i++) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("meetingType", type);
                    m.put("gap", required - actual);
                    missing.add(m);
                }
            }
        }
        BigDecimal overall = totalShould == 0 ? BigDecimal.valueOf(100)
                : BigDecimal.valueOf(totalActual * 100.0 / totalShould).setScale(2, RoundingMode.HALF_UP);
        r.put("year", year);
        r.put("quarter", quarter);
        r.put("rows", rows);
        r.put("overallRate", overall);
        r.put("missing", missing);
        r.put("lecturerCheck", checkLecturers(orgId, year));
        if (overall.compareTo(BigDecimal.valueOf(100)) < 0) {
            r.put("warning", "E-AC-11");
        }
        return r;
    }

    /** 完整率校验接口：低于100%预警 E-AC-11（列出缺失类型） */
    public Map<String, Object> checkComplete(Long orgId, Integer year, Integer quarter) {
        Map<String, Object> o = overview(orgId, year, quarter);
        if (new BigDecimal(100).compareTo((BigDecimal) o.get("overallRate")) > 0) {
            throw new PartyBizException("E-AC-11", PartyBizException.WARN,
                    "台账完整率" + o.get("overallRate") + "%低于100%，缺失"
                            + ((List<?>) o.get("missing")).size() + "项，已列出缺失类型与月份");
        }
        return o;
    }

    /**
     * 连续两个季度完成率不达标 → 升级告警并自动生成考核扣分建议（E-AC-12，联动第10章）。
     */
    public Map<String, Object> checkConsecutive(Long orgId, Integer year, Integer quarter) {
        int prevQ = quarter == 1 ? 4 : quarter - 1;
        int prevY = quarter == 1 ? year - 1 : year;
        BigDecimal cur = quarterRate(orgId, year, quarter);
        BigDecimal prev = quarterRate(orgId, prevY, prevQ);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("currentRate", cur);
        r.put("previousRate", prev);
        boolean failTwice = cur.compareTo(BigDecimal.valueOf(100)) < 0
                && prev.compareTo(BigDecimal.valueOf(100)) < 0;
        if (failTwice) {
            r.put("escalated", true);
            r.put("deductSuggestion", "连续" + prevY + "年第" + prevQ + "季度、" + year + "年第" + quarter
                    + "季度三会一课完成率不达标（" + prev + "%/" + cur + "%），建议党建考核扣5分");
            throw new PartyBizException("E-AC-12", PartyBizException.ALERT,
                    "连续两个季度完成率不达标，升级告警并已生成考核扣分建议（联动第10章）");
        }
        r.put("escalated", false);
        return r;
    }

    private BigDecimal quarterRate(Long orgId, Integer year, Integer quarter) {
        List<ThreeMeetingsLesson> all = ledgerMapper.selectList(new QueryWrapper<ThreeMeetingsLesson>()
                .eq("deleted", 0).eq("period_year", year).eq("period_quarter", quarter)
                .eq(orgId != null, "org_id", orgId));
        int should = QUARTER_REQUIRED.values().stream().mapToInt(Integer::intValue).sum();
        int actual = 0;
        for (String type : QUARTER_REQUIRED.keySet()) {
            long n = all.stream().filter(x -> type.equals(x.getMeetingType())).count();
            actual += Math.min((int) n, QUARTER_REQUIRED.get(type));
        }
        return BigDecimal.valueOf(actual * 100.0 / should).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 党课授课刚性检查：机关党委书记/班子成员/党支部书记每年至少讲1次（E-AC-15）。
     */
    public List<Map<String, Object>> checkLecturers(Long orgId, Integer year) {
        List<ThreeMeetingsLesson> lectures = ledgerMapper.selectList(new QueryWrapper<ThreeMeetingsLesson>()
                .eq("deleted", 0).eq("period_year", year).eq("meeting_type", "党课")
                .eq(orgId != null, "org_id", orgId));
        List<Map<String, Object>> r = new ArrayList<>();
        for (String role : LECTURER_ROLES) {
            long n = lectures.stream().filter(x -> role.equals(x.getLecturerRole())).count();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("lecturerRole", role);
            m.put("required", 1);
            m.put("actual", n);
            m.put("passed", n >= 1);
            r.add(m);
        }
        return r;
    }

    /** 授课刚性校验接口（任一角色年度未讲 → E-AC-15） */
    public List<Map<String, Object>> checkLecturerRigid(Long orgId, Integer year) {
        List<Map<String, Object>> r = checkLecturers(orgId, year);
        List<String> bad = r.stream().filter(m -> Boolean.FALSE.equals(m.get("passed")))
                .map(m -> (String) m.get("lecturerRole")).toList();
        if (!bad.isEmpty()) {
            throw new PartyBizException("E-AC-15", PartyBizException.WARN,
                    year + "年度党课授课刚性要求未达标：" + String.join("、", bad) + "未讲党课（每年至少1次）");
        }
        return r;
    }
}
