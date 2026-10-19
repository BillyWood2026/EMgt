package com.example.perf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 继任计划 Entity（/api/success-plan，perf_mgt.succession_plan）
 * 状态：DRAFT(草稿)/SUBMITTED(提交审批)/ACTIVE(生效)/OBSOLETE(作废)
 */
@Data
@TableName("succession_plan")
public class SuccessionPlan {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 计划编号 */
    @TableField("plan_no")
    private String planNo;

    /** 计划名称 */
    @TableField("plan_name")
    private String planName;

    /** 关键岗位 */
    @TableField("position_name")
    private String positionName;

    @TableField("department")
    private String department;

    /** 规划年度 */
    @TableField("plan_year")
    private Integer planYear;

    /** 责任人 */
    @TableField("owner")
    private String owner;

    /** 继任候选人数量 */
    @TableField("successor_count")
    private Integer successorCount;

    /** 状态 DRAFT/SUBMITTED/ACTIVE/OBSOLETE */
    @TableField("status")
    private String status;

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
