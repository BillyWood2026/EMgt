package com.example.partyaffairsmgt.member.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.member.entity.EduCourse;
import com.example.partyaffairsmgt.member.entity.EduHoursStat;
import com.example.partyaffairsmgt.member.entity.EduPlan;
import com.example.partyaffairsmgt.member.entity.EduStudy;
import com.example.partyaffairsmgt.member.entity.EduTeacher;
import com.example.partyaffairsmgt.member.service.EducationTrainingService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 党员教育培训控制器（需求 6.6）。
 */
@RestController
@RequestMapping("/api/party/member/education")
@CrossOrigin(origins = "*")
public class EducationTrainingController {

    @Autowired
    private EducationTrainingService educationService;

    /** 培训计划列表 */
    @GetMapping("/plans")
    public ResponseResult<List<EduPlan>> plans(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer year) {
        return ResponseResult.success(educationService.plans(orgId, year));
    }

    /** 保存培训计划 */
    @PostMapping("/plans")
    public ResponseResult<EduPlan> savePlan(@RequestBody EduPlan plan) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(educationService.savePlan(plan));
    }

    /** 课程库列表 */
    @GetMapping("/courses")
    public ResponseResult<List<EduCourse>> courses() {
        return ResponseResult.success(educationService.courses());
    }

    /** 保存课程 */
    @PostMapping("/courses")
    public ResponseResult<EduCourse> saveCourse(@RequestBody EduCourse course) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(educationService.saveCourse(course));
    }

    /** 师资库列表 */
    @GetMapping("/teachers")
    public ResponseResult<List<EduTeacher>> teachers() {
        return ResponseResult.success(educationService.teachers());
    }

    /** 保存师资 */
    @PostMapping("/teachers")
    public ResponseResult<EduTeacher> saveTeacher(@RequestBody EduTeacher teacher) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(educationService.saveTeacher(teacher));
    }

    /** 在线学习记录 */
    @GetMapping("/study-records")
    public ResponseResult<List<EduStudy>> studyRecords(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Integer year) {
        return ResponseResult.success(educationService.studyRecords(memberId, courseId, year));
    }

    /** 登记在线学习 */
    @PostMapping("/study-records")
    public ResponseResult<EduStudy> addStudyRecord(@RequestBody EduStudy study) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(educationService.addStudyRecord(study));
    }

    /** 学时统计 */
    @GetMapping("/hours-stat")
    public ResponseResult<List<EduHoursStat>> hoursStats(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer year) {
        return ResponseResult.success(educationService.hoursStats(orgId, year));
    }

    /** 生成学时统计 */
    @PostMapping("/hours-stat/generate")
    public ResponseResult<Void> generateHoursStat(@RequestParam Integer year) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        educationService.generateHoursStat(year);
        return ResponseResult.success(null);
    }

    /** 计划完成率 */
    @GetMapping("/plan-completion/{planId}")
    public ResponseResult<Map<String, Object>> planCompletion(@PathVariable Long planId) {
        return ResponseResult.success(educationService.planCompletion(planId));
    }

    /** 学时不足预警 */
    @GetMapping("/training-warning")
    public ResponseResult<List<Map<String, Object>>> trainingWarning(@RequestParam Integer year) {
        return ResponseResult.success(educationService.trainingWarning(year));
    }
}
