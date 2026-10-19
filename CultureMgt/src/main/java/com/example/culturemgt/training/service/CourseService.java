package com.example.culturemgt.training.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.training.entity.Course;
import com.example.culturemgt.training.entity.StudyTask;
import com.example.culturemgt.training.mapper.CourseMapper;
import com.example.culturemgt.training.mapper.StudyTaskMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 文化课程元数据：中台课程对接（播放器跳转中台地址），CCMS 仅维护元数据。 */
@Service
public class CourseService {

    private static final List<String> CATEGORIES = List.of("新人必修", "年度复训", "价值观专题", "红线禁令专题");

    @Autowired private CourseMapper courseMapper;
    @Autowired private StudyTaskMapper taskMapper;
    @Autowired private AuditService audit;

    public List<Course> list(String category, String status) {
        LambdaQueryWrapper<Course> w = new LambdaQueryWrapper<>();
        if (category != null && !category.isBlank()) w.eq(Course::getCourseCategory, category);
        if (status != null && !status.isBlank()) w.eq(Course::getStatus, status);
        w.orderByAsc(Course::getId);
        return courseMapper.selectList(w);
    }

    public Course save(Course c) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        TrainingFlow.require(TrainingFlow.len(c.getCourseName()) >= 2 && TrainingFlow.len(c.getCourseName()) <= 100,
                "01", "课程名称需2-100字");
        TrainingFlow.require(c.getCourseCategory() != null && CATEGORIES.contains(c.getCourseCategory()), "01",
                "课程分类须为 新人必修/年度复训/价值观专题/红线禁令专题");
        TrainingFlow.require(TrainingFlow.len(c.getMidCourseId()) > 0, "01", "关联集团培训中台课程ID必填");
        TrainingFlow.require(c.getCreditHours() != null
                && c.getCreditHours().compareTo(BigDecimal.ZERO) >= 0
                && c.getCreditHours().compareTo(new BigDecimal("40")) <= 0, "02", "学时须在0-40之间");
        Long dup = courseMapper.selectCount(new LambdaQueryWrapper<Course>()
                .eq(Course::getMidCourseId, c.getMidCourseId())
                .eq(Course::getDeleted, 0)
                .ne(c.getId() != null, Course::getId, c.getId() == null ? -1L : c.getId()));
        TrainingFlow.require(dup == 0, "03", "该培训中台课程ID已被其他课程关联");
        if (c.getId() == null) {
            c.setStatus("已启用");
            c.setRequiredFlag(c.getRequiredFlag() == null ? 1 : c.getRequiredFlag());
            c.setOperator(CulRole.currentRoles().stream().sorted().findFirst().orElse("C03"));
            c.setCreateTime(LocalDateTime.now());
            courseMapper.insert(c);
            audit.log("NEW", "cul_course", c.getId(), "新增文化课程 " + c.getCourseName());
        } else {
            Course old = courseMapper.selectById(c.getId());
            TrainingFlow.require(old != null, "01", "课程不存在");
            c.setStatus(old.getStatus());
            c.setUpdateTime(LocalDateTime.now());
            courseMapper.updateById(c);
            audit.log("EDIT", "cul_course", c.getId(), "调整文化课程 " + c.getCourseName());
        }
        return c;
    }

    public void toggle(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        Course c = courseMapper.selectById(id);
        TrainingFlow.require(c != null, "01", "课程不存在");
        Course u = new Course();
        u.setId(id);
        u.setStatus("已启用".equals(c.getStatus()) ? "已停用" : "已启用");
        u.setUpdateTime(LocalDateTime.now());
        courseMapper.updateById(u);
        audit.log("TOGGLE", "cul_course", id, "文化课程" + u.getStatus());
    }

    /** 删除课程：被学习任务引用时阻断（E-TR-14），引导先处理引用任务。 */
    public void delete(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        Course c = courseMapper.selectById(id);
        TrainingFlow.require(c != null, "01", "课程不存在");
        Long refs = taskMapper.selectCount(new LambdaQueryWrapper<StudyTask>()
                .eq(StudyTask::getCourseId, id).eq(StudyTask::getDeleted, 0));
        TrainingFlow.require(refs == 0, "14", "课程已被 " + refs + " 个学习任务引用，禁止删除；可先停用课程");
        courseMapper.deleteById(id);
        audit.log("DELETE", "cul_course", id, "删除文化课程 " + c.getCourseName());
    }
}
