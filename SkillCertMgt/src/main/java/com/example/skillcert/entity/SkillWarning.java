package com.example.skillcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 技能预警（skill-certification/SkillWarning.vue）
 */
@Data
@TableName("skill_warning")
public class SkillWarning {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String warningType;
    private String warningTypeText;
    private String staffNo;
    private String userName;
    private String orgName;
    private Long skillTypeId;
    private String skillTypeName;
    private String currentLevel;
    private String targetLevel;
    private Integer warningDays;
    private String warningContent;
    private String deadline;

    /** PENDING/PROCESSING/PROCESSED/EXEMPTED */
    private String handleStatus;

    /** 处理状态文本（展示） */
    @TableField(exist = false)
    private String handleStatusText;

    private String handlerName;
    private String handleTime;
    private String processType;
    private String processRemark;

    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    @Version
    private Integer version = 1;

    @TableLogic
    private Integer deleted = 0;
}
