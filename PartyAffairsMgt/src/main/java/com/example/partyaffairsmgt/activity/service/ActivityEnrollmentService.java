package com.example.partyaffairsmgt.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.activity.entity.ActivityEnrollment;
import com.example.partyaffairsmgt.activity.entity.ActivityPlan;
import com.example.partyaffairsmgt.activity.mapper.ActivityEnrollmentMapper;
import com.example.partyaffairsmgt.activity.mapper.ActivityPlanMapper;
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
 * 活动发布与报名签到服务（需求 9.2）。
 * 签到方式：现场扫码/定位/代签/离线补签；一人一活动仅一次签到；
 * 补签须注明原因并审批；定位超半径（默认500米）标记待核实；签到通道随活动开闭。
 * 错误码：E-AC-04 重复签到；E-AC-05 补签无原因；E-AC-06 定位超半径；E-AC-07 签到通道未开启。
 */
@Service
public class ActivityEnrollmentService {

    /** 定位签到允许半径（米，参数化） */
    public static final int GEO_RADIUS_M = 500;

    @Autowired
    private ActivityEnrollmentMapper enrollMapper;
    @Autowired
    private ActivityPlanMapper planMapper;
    @Autowired
    private AuditService auditService;

    public List<ActivityEnrollment> list(Long activityId, String signStatus, String memberName) {
        QueryWrapper<ActivityEnrollment> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (activityId != null) qw.eq("activity_id", activityId);
        if (signStatus != null && !signStatus.isBlank()) qw.eq("sign_status", signStatus);
        if (memberName != null && !memberName.isBlank()) qw.like("member_name", memberName);
        qw.orderByDesc("create_time");
        return enrollMapper.selectList(qw);
    }

    /** 报名 */
    @Transactional
    public ActivityEnrollment enroll(Long activityId, Long memberId, String memberName) {
        ActivityPlan plan = planMapper.selectById(activityId);
        if (plan == null || plan.getDeleted() == 1) {
            throw PartyBizException.block("E-AC-07", "活动不存在");
        }
        if (!"已发布".equals(plan.getPublishStatus())) {
            throw PartyBizException.block("E-AC-07", "活动尚未发布，不可报名");
        }
        if (plan.getEnrollDeadline() != null && LocalDateTime.now().isAfter(plan.getEnrollDeadline())) {
            throw PartyBizException.block("E-AC-07", "已过报名截止时间");
        }
        Long exists = enrollMapper.selectCount(new QueryWrapper<ActivityEnrollment>()
                .eq("deleted", 0).eq("activity_id", activityId).eq("member_id", memberId));
        if (exists != null && exists > 0) {
            throw PartyBizException.block("E-AC-04", "该党员已报名，请勿重复报名");
        }
        ActivityEnrollment e = new ActivityEnrollment();
        e.setActivityId(activityId);
        e.setActivityTitle(plan.getTitle());
        e.setOrgId(plan.getOrgId());
        e.setOrgName(plan.getOrgName());
        e.setMemberId(memberId);
        e.setMemberName(memberName);
        e.setEnrollTime(LocalDateTime.now());
        e.setAttendStatus("已报名");
        e.setSignStatus("未签到");
        e.setCreateTime(LocalDateTime.now());
        enrollMapper.insert(e);
        return e;
    }

    /**
     * 签到。
     *
     * @param force 演示用：跳过通道时间窗校验（活动非当天时由前端传 true 模拟现场开启）
     */
    @Transactional
    public ActivityEnrollment sign(Long activityId, Long memberId, String method, Integer distanceM, boolean force) {
        ActivityPlan plan = planMapper.selectById(activityId);
        if (plan == null) {
            throw PartyBizException.block("E-AC-07", "活动不存在");
        }
        // 签到通道：活动开始后开启、结束后关闭（演示活动非当天时允许 force 模拟）
        if (!force && plan.getPlanDate() != null && !plan.getPlanDate().equals(LocalDate.now())) {
            throw PartyBizException.block("E-AC-07",
                    plan.getPlanDate().isAfter(LocalDate.now()) ? "活动尚未开始，签到通道未开启" : "活动已结束，签到通道已关闭");
        }
        ActivityEnrollment e = enrollMapper.selectOne(new QueryWrapper<ActivityEnrollment>()
                .eq("deleted", 0).eq("activity_id", activityId).eq("member_id", memberId).last("LIMIT 1"));
        if (e == null) {
            throw PartyBizException.block("E-AC-07", "未找到报名记录，请先报名");
        }
        // 同一党员同一活动仅计一次签到
        if ("已签到".equals(e.getSignStatus())) {
            throw PartyBizException.block("E-AC-04", "重复签到拦截：该党员本活动已签到");
        }
        e.setSignMethod(method);
        e.setSignInTime(LocalDateTime.now());
        e.setAttendStatus("已参加");
        e.setDistanceM(distanceM);
        // 定位签到超出半径 → 标记待核实（WARN，不阻断）
        if ("定位".equals(method) && distanceM != null && distanceM > GEO_RADIUS_M) {
            e.setSignStatus("待核实");
            enrollMapper.updateById(e);
            throw new PartyBizException("E-AC-06", PartyBizException.WARN,
                    "定位距活动地点" + distanceM + "米，超出" + GEO_RADIUS_M + "米半径，已标记为待核实");
        }
        e.setSignStatus("已签到");
        e.setMakeUpAudit(null);
        e.setUpdateTime(LocalDateTime.now());
        enrollMapper.updateById(e);
        auditService.log("三会一课", "签到", e.getId(), plan.getTitle() + "/" + memberId);
        return e;
    }

    /** 离线补签（原因必填，可配置需审批：默认提交后待审） */
    @Transactional
    public ActivityEnrollment makeup(Long enrollId, String reason, boolean directPass) {
        if (reason == null || reason.isBlank()) {
            throw PartyBizException.block("E-AC-05", "补签必须注明原因，未注明原因阻断");
        }
        ActivityEnrollment e = enrollMapper.selectById(enrollId);
        if (e == null || e.getDeleted() == 1) {
            throw PartyBizException.block("E-AC-05", "报名记录不存在");
        }
        if ("已签到".equals(e.getSignStatus())) {
            throw PartyBizException.block("E-AC-04", "该党员已签到，无需补签");
        }
        e.setSignMethod("离线补签");
        e.setMakeUpReason(reason);
        if (directPass) {
            e.setMakeUpAudit("通过");
            e.setSignStatus("已签到");
            e.setSignInTime(LocalDateTime.now());
            e.setAttendStatus("已参加");
        } else {
            e.setMakeUpAudit("待审");
            e.setSignStatus("待核实");
        }
        e.setUpdateTime(LocalDateTime.now());
        enrollMapper.updateById(e);
        auditService.log("三会一课", "补签申请", enrollId, reason);
        return e;
    }

    /** 补签审批 */
    @Transactional
    public ActivityEnrollment auditMakeup(Long enrollId, boolean pass) {
        ActivityEnrollment e = enrollMapper.selectById(enrollId);
        if (e == null || !"待审".equals(e.getMakeUpAudit())) {
            throw PartyBizException.block("E-AC-05", "无待审批的补签申请");
        }
        e.setMakeUpAudit(pass ? "通过" : "驳回");
        if (pass) {
            e.setSignStatus("已签到");
            e.setSignInTime(LocalDateTime.now());
            e.setAttendStatus("已参加");
        } else {
            e.setSignStatus("未签到");
        }
        e.setUpdateTime(LocalDateTime.now());
        enrollMapper.updateById(e);
        return e;
    }

    /** 签到统计：到会率=实际签到人数/应参加人数（实时汇总到活动记录） */
    public Map<String, Object> stat(Long activityId) {
        ActivityPlan plan = planMapper.selectById(activityId);
        List<ActivityEnrollment> list = enrollMapper.selectList(new QueryWrapper<ActivityEnrollment>()
                .eq("deleted", 0).eq("activity_id", activityId));
        long signed = list.stream().filter(x -> "已签到".equals(x.getSignStatus())).count();
        long pending = list.stream().filter(x -> "待核实".equals(x.getSignStatus())).count();
        long enrolled = list.size();
        int expect = plan != null && plan.getExpectCount() != null ? plan.getExpectCount()
                : (int) Math.max(enrolled, signed);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("activityId", activityId);
        r.put("activityTitle", plan == null ? null : plan.getTitle());
        r.put("expectCount", expect);
        r.put("enrolledCount", enrolled);
        r.put("signedCount", signed);
        r.put("pendingCount", pending);
        r.put("attendRate", expect == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(signed * 100.0 / expect).setScale(2, RoundingMode.HALF_UP));
        r.put("signMethods", plan == null ? null : plan.getSignMethod());
        return r;
    }
}
