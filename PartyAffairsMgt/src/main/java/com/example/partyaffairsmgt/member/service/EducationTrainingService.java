package com.example.partyaffairsmgt.member.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.member.entity.EduCourse;
import com.example.partyaffairsmgt.member.entity.EduHoursStat;
import com.example.partyaffairsmgt.member.entity.EduPlan;
import com.example.partyaffairsmgt.member.entity.EduStudy;
import com.example.partyaffairsmgt.member.entity.EduTeacher;
import com.example.partyaffairsmgt.member.mapper.EduCourseMapper;
import com.example.partyaffairsmgt.member.mapper.EduHoursStatMapper;
import com.example.partyaffairsmgt.member.mapper.EduPlanMapper;
import com.example.partyaffairsmgt.member.mapper.EduStudyMapper;
import com.example.partyaffairsmgt.member.mapper.EduTeacherMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 党员教育培训服务（需求 6.6）。
 * 培训计划、课程库、师资库、在线学习、学时统计。
 */
@Service
public class EducationTrainingService {

    @Autowired
    private EduPlanMapper planMapper;
    @Autowired
    private EduCourseMapper courseMapper;
    @Autowired
    private EduTeacherMapper teacherMapper;
    @Autowired
    private EduStudyMapper studyMapper;
    @Autowired
    private EduHoursStatMapper hoursStatMapper;
    @Autowired
    private AuditService auditService;

    /** 培训计划列表 */
    public List<EduPlan> plans(Long orgId, Integer year) {
        QueryWrapper<EduPlan> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (orgId != null) qw.eq("org_id", orgId);
        if (year != null) qw.eq("train_year", year);
        return planMapper.selectList(qw.orderByDesc("train_year"));
    }

    /** 保存培训计划 */
    @Transactional
    public EduPlan savePlan(EduPlan plan) {
        if (plan.getId() == null) {
            plan.setCreator(CurrentHeader.operator());
            plan.setCreateTime(LocalDateTime.now());
            planMapper.insert(plan);
        } else {
            plan.setUpdateTime(LocalDateTime.now());
            planMapper.updateById(plan);
        }
        auditService.log("教育培训", plan.getId() == null ? "新增计划" : "编辑计划", plan.getId(), plan.getPlanName());
        return plan;
    }

    /** 课程库列表 */
    public List<EduCourse> courses() {
        return courseMapper.selectList(
                new QueryWrapper<EduCourse>().eq("deleted", 0).eq("enabled", 1).orderByAsc("id"));
    }

    /** 保存课程 */
    @Transactional
    public EduCourse saveCourse(EduCourse course) {
        if (course.getId() == null) {
            course.setCreator(CurrentHeader.operator());
            course.setCreateTime(LocalDateTime.now());
            courseMapper.insert(course);
        } else {
            course.setUpdateTime(LocalDateTime.now());
            courseMapper.updateById(course);
        }
        auditService.log("教育培训", course.getId() == null ? "新增课程" : "编辑课程", course.getId(), course.getCourseName());
        return course;
    }

    /** 师资库列表 */
    public List<EduTeacher> teachers() {
        return teacherMapper.selectList(
                new QueryWrapper<EduTeacher>().eq("deleted", 0).eq("enabled", 1).orderByAsc("id"));
    }

    /** 保存师资 */
    @Transactional
    public EduTeacher saveTeacher(EduTeacher teacher) {
        if (teacher.getId() == null) {
            teacher.setCreator(CurrentHeader.operator());
            teacher.setCreateTime(LocalDateTime.now());
            teacherMapper.insert(teacher);
        } else {
            teacher.setUpdateTime(LocalDateTime.now());
            teacherMapper.updateById(teacher);
        }
        auditService.log("教育培训", teacher.getId() == null ? "新增师资" : "编辑师资", teacher.getId(), teacher.getTeacherName());
        return teacher;
    }

    /** 在线学习记录 */
    public List<EduStudy> studyRecords(Long memberId, Long courseId, Integer year) {
        QueryWrapper<EduStudy> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (memberId != null) qw.eq("member_id", memberId);
        if (courseId != null) qw.eq("course_id", courseId);
        if (year != null) qw.apply("YEAR(study_date) = {0}", year);
        return studyMapper.selectList(qw.orderByDesc("study_date"));
    }

    /** 登记在线学习 */
    @Transactional
    public EduStudy addStudyRecord(EduStudy study) {
        // 进度<80%不计学时（E-MB-13）
        if (study.getProgress() != null && study.getProgress() < 80) {
            study.setValidFlag(0);
            study.setScoreHours(BigDecimal.ZERO);
        } else {
            study.setValidFlag(1);
            // 计算实际计入学时
            if (study.getStudyMinutes() != null && study.getCourseMinutes() != null && study.getCourseMinutes() > 0) {
                // 按学习比例计算学时（1小时=60分钟）
                BigDecimal hours = new BigDecimal(study.getStudyMinutes())
                        .divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
                study.setScoreHours(hours);
            } else {
                study.setScoreHours(BigDecimal.ZERO);
            }
        }
        // 重复学习去重（E-MB-14）：同一天同一人同课程只保留一条有效
        EduStudy existing = studyMapper.selectOne(
                new QueryWrapper<EduStudy>()
                        .eq("member_id", study.getMemberId())
                        .eq("course_id", study.getCourseId())
                        .eq("study_date", study.getStudyDate())
                        .eq("deleted", 0));
        if (existing != null) {
            // 更新为更高进度的记录
            if (study.getProgress() != null && existing.getProgress() != null
                    && study.getProgress() > existing.getProgress()) {
                study.setId(existing.getId());
                study.setUpdateTime(LocalDateTime.now());
                studyMapper.updateById(study);
                auditService.log("教育培训", "更新学习记录", study.getId(), study.getCourseName() + ", 进度:" + study.getProgress());
                return study;
            } else {
                throw PartyBizException.block("E-MB-14", "同课程同日期已存在学习记录，重复学习已去重");
            }
        }
        study.setCreateTime(LocalDateTime.now());
        studyMapper.insert(study);
        auditService.log("教育培训", "新增学习记录", study.getId(), study.getCourseName() + ", 进度:" + study.getProgress());
        return study;
    }

    /** 学时统计 */
    public List<EduHoursStat> hoursStats(Long orgId, Integer year) {
        QueryWrapper<EduHoursStat> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (orgId != null) qw.eq("org_id", orgId);
        if (year != null) qw.eq("stat_year", year);
        return hoursStatMapper.selectList(qw.orderByDesc("total_hours"));
    }

    /** 生成学时统计 */
    @Transactional
    public void generateHoursStat(Integer year) {
        // 按人汇总当年学时
        QueryWrapper<EduStudy> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        qw.eq("valid_flag", 1);
        qw.apply("YEAR(study_date) = {0}", year);
        List<EduStudy> studies = studyMapper.selectList(qw);
        Map<Long, BigDecimal> memberHours = new HashMap<>();
        Map<Long, EduHoursStat> existing = new HashMap<>();
        for (EduStudy s : studies) {
            memberHours.merge(s.getMemberId(), s.getScoreHours() == null ? BigDecimal.ZERO : s.getScoreHours(), BigDecimal::add);
        }
        // 获取现有记录
        List<EduHoursStat> stats = hoursStatMapper.selectList(
                new QueryWrapper<EduHoursStat>().eq("stat_year", year).eq("deleted", 0));
        for (EduHoursStat s : stats) {
            existing.put(s.getMemberId(), s);
        }
        // 更新或插入
        for (Map.Entry<Long, BigDecimal> e : memberHours.entrySet()) {
            EduHoursStat stat = existing.get(e.getKey());
            if (stat == null) {
                stat = new EduHoursStat();
                stat.setMemberId(e.getKey());
                stat.setStatYear(year);
                stat.setCreateTime(LocalDateTime.now());
            }
            stat.setTotalHours(e.getValue());
            stat.setUpdateTime(LocalDateTime.now());
            // 从学习记录获取信息
            EduStudy sample = studyMapper.selectOne(
                    new QueryWrapper<EduStudy>().eq("member_id", e.getKey()).last("limit 1"));
            if (sample != null) {
                stat.setMemberName(sample.getMemberName());
                stat.setOrgId(sample.getOrgId());
                stat.setOrgName(sample.getOrgName());
            }
            // 计算达标率（目标默认32学时）
            BigDecimal target = stat.getTargetHours() == null || stat.getTargetHours().compareTo(BigDecimal.ZERO) == 0
                    ? new BigDecimal(32) : stat.getTargetHours();
            stat.setTargetHours(target);
            BigDecimal rate = e.getValue().multiply(new BigDecimal(100)).divide(target, 2, RoundingMode.HALF_UP);
            stat.setCompletionRate(rate);
            stat.setStatus(e.getValue().compareTo(target) >= 0 ? "达标" : "未达标");
            if (stat.getId() == null) {
                hoursStatMapper.insert(stat);
            } else {
                hoursStatMapper.updateById(stat);
            }
        }
        auditService.log("教育培训", "生成学时统计", year, "年度:" + year);
    }

    /** 计划完成率 */
    public Map<String, Object> planCompletion(Long planId) {
        EduPlan plan = planMapper.selectById(planId);
        if (plan == null || plan.getDeleted() == 1) {
            throw PartyBizException.block("E-MB-13", "培训计划不存在");
        }
        // 统计完成人数
        Long completed = studyMapper.selectCount(
                new QueryWrapper<EduStudy>()
                        .eq("deleted", 0)
                        .eq("valid_flag", 1)
                        .apply("YEAR(study_date) = {0}", plan.getTrainYear()));
        Integer target = plan.getTargetCount() == null || plan.getTargetCount() == 0 ? 1 : plan.getTargetCount();
        BigDecimal rate = new BigDecimal(completed).multiply(new BigDecimal(100))
                .divide(new BigDecimal(target), 2, RoundingMode.HALF_UP);
        Map<String, Object> result = new HashMap<>();
        result.put("planId", planId);
        result.put("planName", plan.getPlanName());
        result.put("targetCount", target);
        result.put("completedCount", completed);
        result.put("completionRate", rate);
        result.put("status", plan.getStatus());
        return result;
    }

    /** 学时不足预警 */
    public List<Map<String, Object>> trainingWarning(Integer year) {
        List<EduHoursStat> stats = hoursStats(null, year);
        return stats.stream()
                .filter(s -> "未达标".equals(s.getStatus()))
                .map(s -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("memberId", s.getMemberId());
                    map.put("memberName", s.getMemberName());
                    map.put("orgName", s.getOrgName());
                    map.put("totalHours", s.getTotalHours());
                    map.put("targetHours", s.getTargetHours());
                    map.put("completionRate", s.getCompletionRate());
                    map.put("message", "学时不足，请加强学习");
                    return map;
                }).collect(Collectors.toList());
    }
}
