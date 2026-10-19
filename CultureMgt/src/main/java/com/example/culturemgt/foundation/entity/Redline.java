package com.example.culturemgt.foundation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("cul_redline")
public class Redline {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String category;
    private String description;
    private String violationLevel;
    private String punishStd;
    private Integer vetoFlag;
    private String scopeText;
    private String status;
    private Integer stage;
    private String approvalLog;
    private Integer relatedCount;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
