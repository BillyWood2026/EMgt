package com.example.culturemgt.foundation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("cul_glossary")
public class Glossary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String valueTag;
    private String positiveText;
    private String negativeText;
    private String faq;
    private String applyOrg;
    private String status;
    private Integer stage;
    private String approvalLog;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
