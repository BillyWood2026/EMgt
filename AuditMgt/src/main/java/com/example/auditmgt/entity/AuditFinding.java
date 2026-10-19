package com.example.auditmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("audit_finding")
public class AuditFinding {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String projectName;
    private String title;
    private String level;
    private String description;
    private String recommendation;
    private String rectificationMeasure;
    private LocalDate deadline;
    private String responsible;
    private String status;
    private LocalDateTime resolveTime;
    private LocalDateTime rectifyTime;
    private String reviewer;
    private String reviewComment;
    private LocalDateTime reviewTime;
    private LocalDateTime closeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
