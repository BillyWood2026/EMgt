package com.example.culturemgt.honor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("cul_honor_apply")
public class HonorApply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String projectName;
    private String applyType;
    private String candidate;
    private String userDept;
    private String deeds;
    private String attachments;
    private BigDecimal cultureScore;
    private String status;
    private Integer stage;
    private String approvalLog;
    private String reviewOpinion;
    private LocalDate publicityStart;
    private LocalDate publicityEnd;
    private String objections;
    private LocalDate awardTime;
    private String certFiles;
    private Integer syncCase;
    private Long caseId;
    private String contributor;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
