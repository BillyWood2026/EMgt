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
 * 培养工程学员（/api/training-monitoring/students，training_student）
 * 状态：ENROLLED(已报名)/IN_PROGRESS(培养中)/COMPLETED(已结业)/QUITTED(退出)
 */
@Data
@TableName("training_student")
public class TrainingStudent {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("project_id")
    private Long projectId;

    @TableField("student_no")
    private String studentNo;

    @TableField("student_name")
    private String studentName;

    @TableField("department")
    private String department;

    @TableField("enroll_status")
    private String enrollStatus;

    @TableField("enroll_time")
    private String enrollTime;

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
