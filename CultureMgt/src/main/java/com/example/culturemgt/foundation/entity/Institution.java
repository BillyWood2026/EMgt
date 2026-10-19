package com.example.culturemgt.foundation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("cul_institution")
public class Institution {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String docName;
    private String docType;
    private String applyOrg;
    private String applyPost;
    private LocalDate effectDate;
    private LocalDate expireDate;
    private String version;
    private String dept;
    private String summary;
    private String keyClause;
    private String fileUrl;
    private String status;
    private Integer stage;
    private String approvalLog;
    private String reviseReason;
    private Long parentId;
    private Integer shouldCount;
    private Integer learnedCount;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
