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
 * 认证计划（skill-certification/SkillPlan.vue）
 */
@Data
@TableName("skill_plan")
public class SkillPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 计划名称 */
    private String planName;

    /** 认证年度 */
    private Integer planYear;

    /** 覆盖工种ID 逗号分隔（DB 存储） */
    @TableField("skill_type_ids")
    private String skillTypeIdsStr;

    /** 覆盖工种ID 数组（前端交互） */
    @TableField(exist = false)
    private List<Long> skillTypeIds;

    /** 覆盖等级 逗号分隔（DB 存储） */
    @TableField("skill_levels")
    private String skillLevelsStr;

    /** 覆盖等级 数组（前端交互） */
    @TableField(exist = false)
    private List<String> skillLevels;

    /** 覆盖工种名称 逗号分隔（展示） */
    @TableField("skill_types")
    private String skillTypesStr;

    /** 覆盖工种名称 数组（展示） */
    @TableField(exist = false)
    private List<String> skillTypes;

    private String applyStartTime;
    private String applyEndTime;
    private String examTime;
    private Integer joinNum;
    private Integer passNum;

    /** NOT_STARTED/APPLYING/EXAMINING/PUBLIC/ARCHIVED */
    private String planStatus;

    private Long managerUserId;
    private String managerName;

    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    @Version
    private Integer version = 1;

    @TableLogic
    private Integer deleted = 0;
}
