package com.example.salary.entity;

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
 * 岗位评估 Entity
 */
@Data
@TableName("job_position")
public class JobPosition {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("position_name")
    private String positionName;

    @TableField("position_code")
    private String positionCode;

    @TableField("department")
    private String department;

    @TableField("grade_id")
    private Long gradeId;

    @TableField("grade_name")
    private String gradeName;

    @TableField("job_value")
    private Integer jobValue;

    @TableField("evaluation_score")
    private BigDecimal evaluationScore;

    @TableField("responsibilities")
    private String responsibilities;

    @TableField("requirements")
    private String requirements;

    @TableField("status")
    private String status;

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
