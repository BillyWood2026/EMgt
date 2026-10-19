package com.example.skillcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("skill")
public class Skill {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("skill_name")
    private String skillName;

    @TableField("skill_code")
    private String skillCode;

    @TableField("category")
    private String category;

    @TableField("level")
    private String level;

    @TableField("description")
    private String description;

    @TableField("standard")
    private String standard;

    @TableField("status")
    private String status;

    @TableField("create_by")
    private String createBy;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_by")
    private String updateBy;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @Version
    @TableField("version")
    private Integer version;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
