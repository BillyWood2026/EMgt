package com.example.perf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 绩效结果归档 Entity（对齐前端 Result.vue：/api/perf/archive）
 */
@Data
@TableName("perf_archive")
public class PerfArchive {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 员工姓名 */
    @TableField("employee_name")
    private String employeeName;

    /** 部门 */
    @TableField("department")
    private String department;

    /** 岗位 */
    @TableField("position")
    private String position;

    /** 评估周期，如 2026Q2 */
    @TableField("period")
    private String period;

    /** 评估得分 */
    @TableField("score")
    private BigDecimal score;

    /** 评级 A/B/C/D */
    @TableField("grade")
    private String grade;

    /** 排名（数据库列 rank_no，RANK 为 MySQL8 保留字；JSON 字段仍输出 rank） */
    @TableField("rank_no")
    private Integer rank;

    /** 评审人 */
    @TableField("reviewer")
    private String reviewer;

    /** 评语 */
    @TableField("comment")
    private String comment;

    /** 申诉状态，如 已受理 */
    @TableField("appeal_status")
    private String appealStatus;

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
