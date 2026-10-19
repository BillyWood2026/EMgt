package com.example.auditmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("audit_plan")
public class AuditPlan {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String planNo;
    private String name;
    private String type;
    private String scope;
    private String objective;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String auditor;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
