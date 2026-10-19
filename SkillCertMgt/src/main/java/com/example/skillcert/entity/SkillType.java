package com.example.skillcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工种配置（skill-certification/SkillStandard.vue）
 */
@Data
@TableName("skill_type")
public class SkillType {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工种名称 */
    private String skillTypeName;

    /** 工种分类 TECH/OPERATE/PROFESSIONAL */
    private String skillCategory;

    /** 开放等级 逗号分隔（DB 存储） */
    @TableField("enabled_levels")
    private String enabledLevelsStr;

    /** 开放等级 数组（前端交互） */
    @TableField(exist = false)
    private List<String> enabledLevels;

    /** 申报状态 1开启 0关闭 */
    private Integer applyStatus;

    /** 证书有效期(月) */
    private Integer certValidMonth;

    /** 预警提前天数 */
    private Integer warnDays;

    /** 强制持证 */
    private Integer forceCert;

    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    @Version
    private Integer version = 1;

    @TableLogic
    private Integer deleted = 0;
}
