package com.example.culturemgt.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.activity.entity.ActivitySign;
import com.example.culturemgt.activity.entity.CultureActivity;
import com.example.culturemgt.activity.mapper.ActivitySignMapper;
import com.example.culturemgt.activity.mapper.CultureActivityMapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.common.cul.CurrentHeader;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 活动报名与签到；signed_count/checked_count 实时回写活动主表。 */
@Service
public class SignService {

    @Autowired private ActivitySignMapper signMapper;
    @Autowired private CultureActivityMapper actMapper;
    @Autowired private AuditService audit;

    public List<ActivitySign> roster(Long activityId) {
        return signMapper.selectList(new LambdaQueryWrapper<ActivitySign>()
                .eq(ActivitySign::getActivityId, activityId).orderByDesc(ActivitySign::getId));
    }

    public List<ActivitySign> mine(String userName) {
        return signMapper.selectList(new LambdaQueryWrapper<ActivitySign>()
                .eq(ActivitySign::getUserName, userName).orderByDesc(ActivitySign::getId));
    }

    /** 员工报名。 */
    public void sign(Long activityId, String userDept) {
        CultureActivity a = mustAct(activityId);
        ActivityFlow.require("已发布".equals(a.getStatus()), "02", "当前活动不在报名中（状态：" + a.getStatus() + "）");
        ActivityFlow.require(a.getSignDeadline() == null || !LocalDateTime.now().isAfter(a.getSignDeadline()),
                "02", "已过报名截止时间");
        String user = CurrentHeader.operator();
        ActivitySign s = signMapper.selectOne(new LambdaQueryWrapper<ActivitySign>()
                .eq(ActivitySign::getActivityId, activityId).eq(ActivitySign::getUserName, user).last("limit 1"));
        if (s == null) {
            int signed = countSigned(activityId);
            if (a.getMaxSign() != null && a.getMaxSign() > 0 && signed >= a.getMaxSign()) {
                ActivityFlow.require(false, "02", "报名人数已达上限(" + a.getMaxSign() + "人)");
            }
            s = new ActivitySign();
            s.setActivityId(activityId);
            s.setUserName(user);
            s.setUserDept(userDept);
            s.setSignTime(LocalDateTime.now());
            s.setSignStatus("已报名");
            s.setCheckStatus("未签到");
            s.setCreateTime(LocalDateTime.now());
            signMapper.insert(s);
        } else {
            ActivityFlow.require("已取消".equals(s.getSignStatus()), "02", "您已报名该活动，请勿重复报名");
            s.setSignStatus("已报名");
            s.setSignTime(LocalDateTime.now());
            s.setCheckStatus("未签到");
            s.setCheckTime(null);
            s.setCheckBy(null);
            s.setUpdateTime(LocalDateTime.now());
            signMapper.updateById(s);
        }
        writeback(activityId);
        audit.log("SIGN", "cul_activity", activityId, user + " 报名活动");
    }

    /** 员工取消报名（截止前）。 */
    public void cancelSign(Long activityId) {
        CultureActivity a = mustAct(activityId);
        String user = CurrentHeader.operator();
        ActivitySign s = signMapper.selectOne(new LambdaQueryWrapper<ActivitySign>()
                .eq(ActivitySign::getActivityId, activityId).eq(ActivitySign::getUserName, user).last("limit 1"));
        ActivityFlow.require(s != null && "已报名".equals(s.getSignStatus()), "02", "未找到您的有效报名记录");
        ActivityFlow.require(a.getSignDeadline() == null || !LocalDateTime.now().isAfter(a.getSignDeadline()),
                "02", "报名截止后不可取消");
        s.setSignStatus("已取消");
        s.setUpdateTime(LocalDateTime.now());
        signMapper.updateById(s);
        writeback(activityId);
        audit.log("SIGN_CANCEL", "cul_activity", activityId, user + " 取消报名");
    }

    /** 现场签到登记：已签到/迟到/请假；活动须进行中，部门负责人或文化岗操作。 */
    public void check(Long activityId, String userName, String checkStatus) {
        mustAct(activityId);
        CulRole.requireAny(CulRole.C04, CulRole.C02, CulRole.C03, CulRole.C99);
        ActivityFlow.require(List.of("已签到", "迟到", "请假").contains(checkStatus), "01", "签到状态须为 已签到/迟到/请假");
        ActivitySign s = signMapper.selectOne(new LambdaQueryWrapper<ActivitySign>()
                .eq(ActivitySign::getActivityId, activityId).eq(ActivitySign::getUserName, userName).last("limit 1"));
        ActivityFlow.require(s != null, "02", "该员工未报名本活动");
        ActivityFlow.require("已报名".equals(s.getSignStatus()), "02", "该员工报名已取消，不可签到");
        s.setCheckStatus(checkStatus);
        s.setCheckTime(LocalDateTime.now());
        s.setCheckBy(CurrentHeader.operator());
        s.setUpdateTime(LocalDateTime.now());
        signMapper.updateById(s);
        writeback(activityId);
        audit.log("CHECKIN", "cul_activity", activityId, userName + " 签到登记：" + checkStatus);
    }

    private int countSigned(Long activityId) {
        return Math.toIntExact(signMapper.selectCount(new LambdaQueryWrapper<ActivitySign>()
                .eq(ActivitySign::getActivityId, activityId).eq(ActivitySign::getSignStatus, "已报名")));
    }

    private void writeback(Long activityId) {
        int signed = countSigned(activityId);
        int checked = Math.toIntExact(signMapper.selectCount(new LambdaQueryWrapper<ActivitySign>()
                .eq(ActivitySign::getActivityId, activityId)
                .in(ActivitySign::getCheckStatus, "已签到", "迟到")));
        CultureActivity a = new CultureActivity();
        a.setId(activityId);
        a.setSignedCount(signed);
        a.setCheckedCount(checked);
        a.setUpdateTime(LocalDateTime.now());
        actMapper.updateById(a);
    }

    private CultureActivity mustAct(Long id) {
        CultureActivity a = actMapper.selectById(id);
        ActivityFlow.require(a != null, "01", "活动不存在");
        return a;
    }
}
