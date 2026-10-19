package com.example.hrplanning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 培养过程监控进度（/api/training-monitoring/progress，training_progress）
 * 综合进度 = 理论×0.1 + 实践×0.7 + 帮带×0.2（后端计算，四舍五入）。
 * 状态：ONGOING(进行中)/URGING(督办中)/CLOSED(已关闭)
 */
@Data
@TableName("training_progress")
public class TrainingProgress {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("project_id")
    private Long projectId;

    @TableField("student_name")
    private String studentName;

    @TableField("student_no")
    private String studentNo;

    /** 理论学习完成率 */
    @TableField("theory_progress")
    private Integer theoryProgress;

    /** 实践锻炼完成率 */
    @TableField("practice_progress")
    private Integer practiceProgress;

    /** 导师帮带完成率 */
    @TableField("mentor_progress")
    private Integer mentorProgress;

    /** 综合进度（后端计算） */
    @TableField("overall_progress")
    private Integer overallProgress;

    @TableField("status")
    private String status;

    @TableField("close_time")
    private String closeTime;

    @TableField("remark")
    private String remark;

    @TableField("deleted")
    @TableLogic
    private Integer deleted = 0;

    @TableField("version")
    @Version
    private Integer version = 1;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("update_by")
    private String updateBy;
}
