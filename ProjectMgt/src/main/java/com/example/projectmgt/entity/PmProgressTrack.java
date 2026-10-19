package com.example.projectmgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pm_progress_track")
public class PmProgressTrack {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("track_no")
    private String trackNo;

    @TableField("project_id")
    private Long projectId;

    @TableField("project_no")
    private String projectNo;

    @TableField("wbs_task_id")
    private Long wbsTaskId;

    @TableField("plan_duration")
    private Integer planDuration;

    @TableField("actual_duration")
    private Integer actualDuration;

    @TableField("plan_progress")
    private Integer planProgress;

    @TableField("actual_progress")
    private Integer actualProgress;

    @TableField("used_amount")
    private BigDecimal usedAmount;

    @TableField("progress_completion_rate")
    private BigDecimal progressCompletionRate;

    @TableField("progress_deviation")
    private Integer progressDeviation;

    @TableField("deviation_rate")
    private BigDecimal deviationRate;

    @TableField("lag_status")
    private String lagStatus;

    @TableField("lag_reason")
    private String lagReason;

    @TableField("rectify_owner")
    private String rectifyOwner;

    @TableField("rectify_time")
    private LocalDateTime rectifyTime;

    @TableField("close_status")
    private String closeStatus;

    @Version
    @TableField("version")
    private Long version;

    @TableField("snapshot_json")
    private String snapshotJson;

    @TableField("created_by")
    private String createdBy;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_by")
    private String updatedBy;

    @TableField("updated_time")
    private LocalDateTime updatedTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
