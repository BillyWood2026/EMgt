package com.example.partyaffairsmgt.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.activity.entity.ActivityEnrollment;
import com.example.partyaffairsmgt.activity.entity.ActivityRecord;
import com.example.partyaffairsmgt.activity.entity.ActivityResolution;
import com.example.partyaffairsmgt.activity.entity.ThreeMeetingsLesson;
import com.example.partyaffairsmgt.activity.mapper.ActivityEnrollmentMapper;
import com.example.partyaffairsmgt.activity.mapper.ActivityRecordMapper;
import com.example.partyaffairsmgt.activity.mapper.ActivityResolutionMapper;
import com.example.partyaffairsmgt.activity.mapper.ThreeMeetingsLessonMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 活动记录与归档服务（需求 9.3）。
 * 模板化记录（要素/议程/发言摘要/决议）；必填要素缺失不可归档；实到与签到联动校验；
 * 归档自动归集四本台账；补录须说明原因；决议事项按责任人+时限跟踪、超期预警。
 * 错误码：E-AC-08 要素缺失不可归档；E-AC-09 实到签到不一致；E-AC-10 补录无原因；E-AC-16 决议超期。
 */
@Service
public class ActivityRecordService {

    @Autowired
    private ActivityRecordMapper recordMapper;
    @Autowired
    private ActivityEnrollmentMapper enrollMapper;
    @Autowired
    private ActivityResolutionMapper resolutionMapper;
    @Autowired
    private ThreeMeetingsLessonMapper ledgerMapper;
    @Autowired
    private AuditService auditService;

    public List<ActivityRecord> list(Long orgId, String activityType, String status, Integer year) {
        QueryWrapper<ActivityRecord> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (orgId != null) qw.eq("org_id", orgId);
        if (activityType != null && !activityType.isBlank()) qw.eq("activity_type", activityType);
        if (status != null && !status.isBlank()) qw.eq("status", status);
        if (year != null) qw.apply("YEAR(hold_date)={0}", year);
        qw.orderByDesc("hold_date");
        return recordMapper.selectList(qw);
    }

    public ActivityRecord detail(Long id) {
        ActivityRecord r = recordMapper.selectById(id);
        if (r == null || r.getDeleted() == 1) {
            throw PartyBizException.block("E-AC-08", "活动记录不存在");
        }
        return r;
    }

    /** 保存模板化记录 */
    @Transactional
    public ActivityRecord save(ActivityRecord r) {
        if (r.getTitle() == null || r.getTitle().isBlank()) {
            throw PartyBizException.block("E-AC-08", "会议主题必填");
        }
        if (r.getIsBackfill() != null && r.getIsBackfill() == 1
                && (r.getBackfillReason() == null || r.getBackfillReason().isBlank())) {
            throw PartyBizException.block("E-AC-10", "补录（追记）必须说明原因，无原因阻断保存");
        }
        if (r.getStatus() == null) r.setStatus("编辑中");
        r.setUpdateTime(LocalDateTime.now());
        if (r.getId() == null) {
            r.setCreateTime(LocalDateTime.now());
            recordMapper.insert(r);
        } else {
            recordMapper.updateById(r);
        }
        return r;
    }

    /**
     * 归档：完整性逐项校验 + 签到联动校验，通过后自动归集三会一课台账。
     */
    @Transactional
    public ActivityRecord archive(Long id) {
        ActivityRecord r = detail(id);
        // 补录原因
        if (r.getIsBackfill() != null && r.getIsBackfill() == 1
                && (r.getBackfillReason() == null || r.getBackfillReason().isBlank())) {
            throw PartyBizException.block("E-AC-10", "补录记录无原因说明，阻断归档");
        }
        // 必填要素逐项校验
        List<String> missing = new ArrayList<>();
        if (blank(r.getHost())) missing.add("主持人");
        if (blank(r.getRecorder())) missing.add("记录人");
        if (r.getHoldDate() == null) missing.add("会议时间");
        if (blank(r.getLocation())) missing.add("会议地点");
        if (r.getExpectCount() == null) missing.add("应到人数");
        if (r.getAttendCount() == null) missing.add("实到人数");
        if (blank(r.getAgenda())) missing.add("会议议程");
        if (blank(r.getSpeeches())) missing.add("发言摘要/决议事项");
        if (!missing.isEmpty()) {
            throw PartyBizException.block("E-AC-08", "记录必填要素缺失，不可归档，缺失项：" + String.join("、", missing));
        }
        // 实到与签到联动：实到人数 > 签到+补签数 → 提示校验（WARN）
        List<ActivityEnrollment> signList = r.getActivityId() == null ? List.of()
                : enrollMapper.selectList(new QueryWrapper<ActivityEnrollment>()
                        .eq("deleted", 0).eq("activity_id", r.getActivityId())
                        .in("sign_status", "已签到"));
        int signed = signList.size();
        r.setSignCount(signed);
        if (r.getAttendCount() != null && signed > 0 && r.getAttendCount() > signed) {
            throw new PartyBizException("E-AC-09", PartyBizException.WARN,
                    "实到人数(" + r.getAttendCount() + ")大于签到人数(" + signed
                            + ")，请核实是否有未补签人员");
        }

        r.setStatus("已归档");
        r.setArchiveTime(LocalDateTime.now());
        r.setArchivist(CurrentHeader.operator());
        r.setUpdateTime(LocalDateTime.now());
        recordMapper.updateById(r);

        // 自动归集四本台账
        if (r.getInLedger() != null && r.getInLedger() == 1
                && ActivityPlanService.LEDGER_TYPES.contains(r.getActivityType())) {
            ThreeMeetingsLesson exist = ledgerMapper.selectOne(new QueryWrapper<ThreeMeetingsLesson>()
                    .eq("deleted", 0).eq("record_id", r.getId()).last("LIMIT 1"));
            ThreeMeetingsLesson l = exist != null ? exist : new ThreeMeetingsLesson();
            l.setOrgId(r.getOrgId());
            l.setOrgName(r.getOrgName());
            l.setMeetingType(r.getActivityType());
            l.setTitle(r.getTitle());
            l.setHoldDate(r.getHoldDate());
            l.setLocation(r.getLocation());
            l.setHost(r.getHost());
            l.setShouldCount(r.getExpectCount());
            l.setActualCount(r.getAttendCount());
            l.setContent(r.getContent());
            l.setStatus("已归档");
            l.setPlanId(r.getActivityId());
            l.setRecordId(r.getId());
            l.setRecorder(r.getRecorder());
            l.setSignCount(signed);
            l.setIsBackfill(r.getIsBackfill() == null ? 0 : r.getIsBackfill());
            l.setBackfillReason(r.getBackfillReason());
            l.setLecturer(r.getLecturer());
            l.setLecturerRole(r.getLecturerRole());
            l.setArchiveTime(r.getArchiveTime());
            l.setArchivist(r.getArchivist());
            if (r.getHoldDate() != null) {
                l.setPeriodYear(r.getHoldDate().getYear());
                l.setPeriodQuarter((r.getHoldDate().getMonthValue() - 1) / 3 + 1);
                l.setPeriodMonth(r.getHoldDate().getMonthValue());
            }
            if (exist == null) {
                l.setCreateTime(LocalDateTime.now());
                ledgerMapper.insert(l);
            } else {
                l.setUpdateTime(LocalDateTime.now());
                ledgerMapper.updateById(l);
            }
        }
        auditService.log("三会一课", "记录归档", r.getId(), r.getTitle());
        return r;
    }

    // ---------------- 决议事项 ----------------

    public List<ActivityResolution> resolutions(Long recordId) {
        return resolutionMapper.selectList(new QueryWrapper<ActivityResolution>()
                .eq("deleted", 0).eq("record_id", recordId).orderByAsc("deadline"));
    }

    @Transactional
    public ActivityResolution saveResolution(ActivityResolution res) {
        if (res.getTitle() == null || res.getTitle().isBlank()) {
            throw PartyBizException.block("E-AC-08", "决议事项必填");
        }
        if (res.getOwner() == null || res.getDeadline() == null) {
            throw PartyBizException.block("E-AC-08", "责任人与落实时限必填");
        }
        if (res.getStatus() == null) res.setStatus("待落实");
        res.setUpdateTime(LocalDateTime.now());
        if (res.getId() == null) {
            res.setCreateTime(LocalDateTime.now());
            resolutionMapper.insert(res);
        } else {
            resolutionMapper.updateById(res);
        }
        return res;
    }

    @Transactional
    public ActivityResolution finishResolution(Long id, String remark) {
        ActivityResolution res = resolutionMapper.selectById(id);
        if (res == null || res.getDeleted() == 1) {
            throw PartyBizException.block("E-AC-08", "决议事项不存在");
        }
        res.setStatus("已落实");
        res.setFinishTime(LocalDate.now());
        res.setRemark(remark);
        res.setUpdateTime(LocalDateTime.now());
        resolutionMapper.updateById(res);
        auditService.log("三会一课", "决议落实", id, res.getTitle());
        return res;
    }

    /** 超期未落实检查（到期提醒，超期预警并联动考核 E-AC-16） */
    public List<ActivityResolution> checkOverdue() {
        List<ActivityResolution> overdue = resolutionMapper.selectList(new QueryWrapper<ActivityResolution>()
                .eq("deleted", 0).ne("status", "已落实")
                .lt("deadline", LocalDate.now()));
        if (!overdue.isEmpty()) {
            throw new PartyBizException("E-AC-16", PartyBizException.WARN,
                    overdue.size() + "项决议超期未落实，已预警并联动考核");
        }
        return overdue;
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
