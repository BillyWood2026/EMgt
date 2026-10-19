package com.example.partyaffairsmgt.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 民主评议年度计划 */
@Data
@TableName("appraisal_plan")
public class AppraisalPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private String orgName;
    private Integer assessYear;
    private String targetScope;
    /** 实名 / 匿名 */
    private String evalMode;
    /** 优秀比例上限，默认 1/3 */
    private BigDecimal excellentRatio;
    /** 四维权重 JSON */
    private String weightsJson;
    /** 草稿/进行中/已公示/已反馈 */
    private String status;
    private String creator;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
