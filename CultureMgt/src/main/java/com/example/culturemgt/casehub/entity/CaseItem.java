package com.example.culturemgt.casehub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("cul_case")
public class CaseItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String caseTitle;
    private String valueTags;
    private String caseType;
    private LocalDateTime occurTime;
    private String content;
    private String attachments;
    private Integer isPublic;
    private String contributor;
    private String orgName;
    private String status;
    private Integer stage;
    private String approvalLog;
    private String rejectReason;
    private Integer pointBase;
    private Integer pointNews;
    private Integer pointTraining;
    private Integer pointTotal;
    private String pointDetail;
    private Integer refNewsCount;
    private Integer refTrainingCount;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
