package com.example.auditmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "audit_project", autoResultMap = true)
public class AuditProject {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String projectNo;
    private String name;
    private Long planId;
    private String planName;
    private String auditScope;
    private String objective;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String leadAuditor;

    @TableField(value = "team_members", typeHandler = JacksonTypeHandler.class)
    private List<String> teamMembers;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
