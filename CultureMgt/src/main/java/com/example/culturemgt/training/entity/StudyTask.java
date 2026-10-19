package com.example.culturemgt.training.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 文化学习任务（新员工必修/全员复训/部门专项）。 */
@Data
@TableName("cul_study_task")
public class StudyTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskName;
    private String taskType;
    private Long courseId;
    private String courseName;
    private String orgScope;
    private LocalDate deadline;
    private Integer remindDays;
    private String status;
    private String operator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
