package com.example.training.entity;

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
 * 培训记录（training-mgt/Record.vue）
 */
@Data
@TableName("training_record")
public class TrainingRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 记录编号 */
    private String recordNo;

    /** 员工姓名 */
    private String employeeName;

    /** 部门 */
    private String department;

    /** 培训课程 */
    private String courseName;

    /** 培训时间 */
    private LocalDate trainingDate;

    /** 时长(h) */
    private BigDecimal duration;

    /** 培训结果 excellent/pass/fail/ongoing */
    private String result;

    /** 证书编号 */
    private String certificateNo;

    /** 培训机构 */
    private String institution;

    /** 学分 */
    private java.math.BigDecimal credit;

    /** 签到状态 */
    private String signInStatus;

    /** 效果评估 */
    private String evaluation;

    /** 培训说明 */
    private String remark;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    @Version
    private Integer version = 1;

    @TableLogic
    private Integer deleted = 0;
}
