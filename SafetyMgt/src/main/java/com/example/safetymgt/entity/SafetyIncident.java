package com.example.safetymgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("safety_incident")
public class SafetyIncident {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 事故编号 */
    private String incidentNo;
    /** 事故名称 */
    private String incidentName;
    /** 事故等级 */
    private String level;
    /** 事故类型 */
    private String incidentType;
    /** 发生时间 */
    private LocalDateTime occurTime;
    /** 发生地点 */
    private String location;
    /** 伤亡情况 */
    private String casualty;
    /** 经济损失(元) */
    private BigDecimal economicLoss;
    /** 事故描述 */
    private String description;
    /** 状态 */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
