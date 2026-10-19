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
 * 人才台账（/api/talent-ledger，talent_ledger）
 */
@Data
@TableName("talent_ledger")
public class TalentLedger {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("employee_no")
    private String employeeNo;

    @TableField("employee_name")
    private String employeeName;

    @TableField("department")
    private String department;

    @TableField("position")
    private String position;

    @TableField("job_level")
    private String jobLevel;

    /** 任职资格等级 */
    @TableField("qualification_level")
    private String qualificationLevel;

    /** 最近绩效等级 */
    @TableField("perf_grade")
    private String perfGrade;

    /** 九宫格 */
    @TableField("nine_box")
    private String nineBox;

    /** 潜力层级 */
    @TableField("potential_level")
    private String potentialLevel;

    /** 培养完成率 */
    @TableField("training_completion")
    private Integer trainingCompletion;

    /** 继任就绪度 */
    @TableField("succession_readiness")
    private String successionReadiness;

    /** 人才标签 */
    @TableField("talent_label")
    private String talentLabel;

    @TableField("snapshot_date")
    private String snapshotDate;

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
