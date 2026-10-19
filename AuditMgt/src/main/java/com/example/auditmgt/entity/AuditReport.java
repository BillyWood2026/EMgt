package com.example.auditmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("audit_report")
public class AuditReport {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String reportNo;
    private String name;
    private Long projectId;
    private String projectName;
    private Integer findingsCount;
    private String executiveSummary;
    private String conclusion;
    private String author;
    private String status;
    private LocalDateTime reviewTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<AuditFinding> findings;

    @TableLogic
    private Integer deleted;
}
