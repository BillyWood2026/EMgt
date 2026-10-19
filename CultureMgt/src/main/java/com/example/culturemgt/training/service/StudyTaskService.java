package com.example.culturemgt.training.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.common.cul.CurrentHeader;
import com.example.culturemgt.training.entity.Course;
import com.example.culturemgt.training.entity.StudyRecord;
import com.example.culturemgt.training.entity.StudyTask;
import com.example.culturemgt.training.mapper.CourseMapper;
import com.example.culturemgt.training.mapper.StudyRecordMapper;
import com.example.culturemgt.training.mapper.StudyTaskMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 学习任务配置：新员工入职自动分配必修、全员年度复训、部门专项学习；
 * 到期提醒（提前 remind_days 天），到期未完成推送中台消息预警。
 */
@Service
public class StudyTaskService {

    private static final List<String> TYPES = List.of("新员工必修", "全员复训", "部门专项");

    @Autowired private StudyTaskMapper taskMapper;
    @Autowired private StudyRecordMapper recordMapper;
    @Autowired private CourseMapper courseMapper;
    @Autowired private AuditService audit;

    public List<StudyTask> list(String taskType, String status) {
        LambdaQueryWrapper<StudyTask> w = new LambdaQueryWrapper<>();
        if (taskType != null && !taskType.isBlank()) w.eq(StudyTask::getTaskType, taskType);
        if (status != null && !status.isBlank()) w.eq(StudyTask::getStatus, status);
        w.orderByDesc(StudyTask::getId);
        return taskMapper.selectList(w);
    }

    public StudyTask save(StudyTask t) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        TrainingFlow.require(TrainingFlow.len(t.getTaskName()) >= 2 && TrainingFlow.len(t.getTaskName()) <= 100,
                "01", "任务名称需2-100字");
        TrainingFlow.require(TYPES.contains(t.getTaskType()), "04", "任务类型须为 新员工必修/全员复训/部门专项");
        TrainingFlow.require(t.getCourseId() != null, "01", "关联课程必填");
        Course c = courseMapper.selectById(t.getCourseId());
        TrainingFlow.require(c != null && c.getDeleted() == 0, "01", "关联课程不存在");
        TrainingFlow.require("已启用".equals(c.getStatus()), "01", "关联课程已停用，请先启用课程");
        TrainingFlow.require(t.getDeadline() != null, "01", "完成截止日期必填");
        if (t.getRemindDays() == null) t.setRemindDays(7);
        t.setCourseName(c.getCourseName());
        if (t.getId() == null) {
            t.setStatus("进行中");
            t.setOperator(CulRole.currentRoles().stream().sorted().findFirst().orElse("C03"));
            t.setCreateTime(LocalDateTime.now());
            taskMapper.insert(t);
            audit.log("NEW", "cul_study_task", t.getId(), "新增学习任务 " + t.getTaskName());
        } else {
            TrainingFlow.require(taskMapper.selectById(t.getId()) != null, "01", "任务不存在");
            t.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(t);
            audit.log("EDIT", "cul_study_task", t.getId(), "调整学习任务 " + t.getTaskName());
        }
        return t;
    }

    public void toggle(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        StudyTask t = taskMapper.selectById(id);
        TrainingFlow.require(t != null, "01", "任务不存在");
        StudyTask u = new StudyTask();
        u.setId(id);
        u.setStatus("进行中".equals(t.getStatus()) ? "已结束" : "进行中");
        u.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(u);
        audit.log("TOGGLE", "cul_study_task", id, "学习任务" + u.getStatus());
    }

    /** 分配学习对象：批量生成学习记录（同任务同人跳过）。 */
    @Transactional
    public Map<String, Object> assign(Long id, List<Map<String, String>> users) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        StudyTask t = taskMapper.selectById(id);
        TrainingFlow.require(t != null, "01", "任务不存在");
        TrainingFlow.require(users != null && !users.isEmpty(), "01", "分配人员名单不能为空");
        int created = 0, skipped = 0;
        for (Map<String, String> u : users) {
            String name = u.get("userName") == null ? "" : u.get("userName").trim();
            TrainingFlow.require(TrainingFlow.len(name) > 0, "01", "人员账号不能为空");
            Long dup = recordMapper.selectCount(new LambdaQueryWrapper<StudyRecord>()
                    .eq(StudyRecord::getTaskId, id).eq(StudyRecord::getUserName, name)
                    .eq(StudyRecord::getDeleted, 0));
            if (dup > 0) { skipped++; continue; }
            StudyRecord r = new StudyRecord();
            r.setTaskId(id);
            r.setTaskName(t.getTaskName());
            r.setCourseId(t.getCourseId());
            r.setCourseName(t.getCourseName());
            r.setUserName(name);
            r.setUserDept(u.get("userDept") == null ? "" : u.get("userDept").trim());
            r.setProgress(0);
            r.setStatus("未完成");
            r.setSyncFromMid(0);
            r.setCreateTime(LocalDateTime.now());
            recordMapper.insert(r);
            created++;
        }
        audit.log("ASSIGN", "cul_study_task", id, "分配学习任务 " + t.getTaskName() + "：新增" + created + " 人，跳过" + skipped + " 人");
        Map<String, Object> r = new HashMap<>();
        r.put("created", created);
        r.put("skipped", skipped);
        return r;
    }

    /** 任务学习明细：记录 + 完成率。 */
    public Map<String, Object> records(Long id) {
        StudyTask t = taskMapper.selectById(id);
        TrainingFlow.require(t != null, "01", "任务不存在");
        List<StudyRecord> rs = recordMapper.selectList(new LambdaQueryWrapper<StudyRecord>()
                .eq(StudyRecord::getTaskId, id).orderByAsc(StudyRecord::getId));
        long done = rs.stream().filter(r -> "已完成".equals(r.getStatus())).count();
        Map<String, Object> m = new HashMap<>();
        m.put("task", t);
        m.put("records", rs);
        m.put("total", rs.size());
        m.put("done", done);
        m.put("rate", rs.isEmpty() ? 0 : Math.round(done * 10000.0 / rs.size()) / 100.0);
        return m;
    }

    /** 模拟培训中台回写：进度0-100；progress=100 时置已完成并记 finish_time。 */
    public StudyRecord sync(Long id, String userName, Integer progress) {
        TrainingFlow.require(progress != null && progress >= 0 && progress <= 100, "15", "学习进度须在0-100之间");
        StudyRecord r = recordMapper.selectOne(new LambdaQueryWrapper<StudyRecord>()
                .eq(StudyRecord::getTaskId, id).eq(StudyRecord::getUserName, userName)
                .eq(StudyRecord::getDeleted, 0).last("LIMIT 1"));
        TrainingFlow.require(r != null, "01", "该人员未被分配此学习任务");
        StudyRecord u = new StudyRecord();
        u.setId(r.getId());
        u.setProgress(progress);
        u.setSyncFromMid(1);
        if (progress >= 100) {
            u.setStatus("已完成");
            u.setFinishTime(LocalDateTime.now());
        }
        u.setUpdateTime(LocalDateTime.now());
        recordMapper.updateById(u);
        if (progress >= 100) {
            audit.log("SYNC", "cul_study_record", r.getId(), userName + " 完成《" + r.getTaskName() + "》学习（中台回写）");
        }
        return recordMapper.selectById(r.getId());
    }

    /** 我的学习任务与进度（前台学习中心）。 */
    public List<Map<String, Object>> myRecords() {
        String me = CurrentHeader.operator();
        List<StudyRecord> rs = recordMapper.selectList(new LambdaQueryWrapper<StudyRecord>()
                .eq(StudyRecord::getUserName, me).orderByDesc(StudyRecord::getId));
        List<Map<String, Object>> out = new ArrayList<>();
        for (StudyRecord r : rs) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("record", r);
            StudyTask t = taskMapper.selectById(r.getTaskId());
            if (t != null) {
                m.put("deadline", t.getDeadline());
                m.put("taskStatus", t.getStatus());
                m.put("overdue", "未完成".equals(r.getStatus()) && LocalDate.now().isAfter(t.getDeadline()));
            }
            out.add(m);
        }
        return out;
    }

    /** 到期提醒名单：临近截止（deadline-remind_days ≤ 今天 ≤ deadline）与已逾期未完成。 */
    public Map<String, Object> remind(Long id) {
        StudyTask t = taskMapper.selectById(id);
        TrainingFlow.require(t != null, "01", "任务不存在");
        List<StudyRecord> undone = recordMapper.selectList(new LambdaQueryWrapper<StudyRecord>()
                .eq(StudyRecord::getTaskId, id).eq(StudyRecord::getStatus, "未完成").eq(StudyRecord::getDeleted, 0));
        LocalDate today = LocalDate.now();
        List<String> near = new ArrayList<>();
        List<String> overdue = new ArrayList<>();
        for (StudyRecord r : undone) {
            if (t.getDeadline() == null) continue;
            if (today.isAfter(t.getDeadline())) {
                overdue.add(r.getUserName());
            } else if (!today.isBefore(t.getDeadline().minusDays(t.getRemindDays() == null ? 7 : t.getRemindDays()))) {
                near.add(r.getUserName());
            }
        }
        Map<String, Object> m = new HashMap<>();
        m.put("task", t);
        m.put("nearDeadline", near);
        m.put("overdue", overdue);
        m.put("undoneCount", undone.size());
        return m;
    }
}
