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
 * 岗位等级 Entity
 */
@Data
@TableName("job_grade")
public class JobGrade {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("grade_name")
    private String gradeName;

    @TableField("grade_code")
    private String gradeCode;

    @TableField("grade_level")
    private Integer gradeLevel;

    @TableField("min_salary")
    private BigDecimal minSalary;

    @TableField("max_salary")
    private BigDecimal maxSalary;

    @TableField("mid_salary")
    private BigDecimal midSalary;

    @TableField("description")
    private String description;

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
