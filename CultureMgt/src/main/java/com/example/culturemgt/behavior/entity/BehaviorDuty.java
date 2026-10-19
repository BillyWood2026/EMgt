package com.example.culturemgt.behavior.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 干部文化履职填报
 */
@Data
@TableName("cul_behavior_duty")
public class BehaviorDuty {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 填报周期 2026Q3/2026-10 */
    private String periodTag;

    /** 干部(C04) */
    private String officer;

    private String dept;

    /** 履职明细 JSON [{key,done,note,attachments}] 5项 */
    private String dutyItems;

    /** 发现的文化短板 */
    private String weakness;

    /** 草稿/待审核/已归档/已驳回 */
    private String status;

    private String reviewOpinion;

    private String reviewer;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime;

    private String approvalLog;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
