package com.example.onlinelearn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习任务/学习进度（learn-to-exam/e-learning/Progress.vue，前缀 /api/learn/tasks）
 */
@Data
@TableName("learn_task")
public class LearnTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 员工姓名 */
    private String employeeName;

    /** 部门 */
    private String department;

    /** 分配课程 */
    private String courseName;

    /** 目标学时 */
    private Integer targetHours;

    /** 截止日期 */
    private LocalDate deadline;

    /** 是否必修 mandatory/elective */
    private String required;

    /** 学习备注 */
    private String remark;

    /** 已学课程数 */
    private Integer courseCount;

    /** 累计学时 */
    private BigDecimal totalHours;

    /** 累计学分 */
    private BigDecimal credit;

    /** 完成进度（百分比） */
    private Integer progress;

    /** 最近学习时间 */
    private String lastStudyTime;

    /** 状态 NOT_STARTED/LEARNING/COMPLETED/OVERDUE */
    private String status;

    /** 指派类型 */
    private String assignType;

    /** 作业状态 */
    private String homeworkStatus;

    /** 考试状态 */
    private String examStatus;

    /** 逾期预警天数 */
    private Integer overdueWarnDays;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    @Version
    private Integer version = 1;

    @TableLogic
    private Integer deleted = 0;
}
