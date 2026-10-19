package com.example.culturemgt.foundation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("cul_creed")
public class Creed {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String creedName;
    private String mission;
    private String vision;
    private String valueGroups;
    private String spirit;
    private String slogan;
    private String applyOrg;
    private LocalDate effectDate;
    private LocalDate expireDate;
    private String version;
    private String status;
    private Integer stage;
    private String approvalLog;
    private String archiveReason;
    private Long parentId;
    private String remark;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
