package com.example.processmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程定义 Entity
 */
@Data
@TableName("process_definition")
public class ProcessDefinition {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("code")
    private String code;

    @TableField("name")
    private String name;

    @TableField("level")
    private String level;

    @TableField("category")
    private String category;

    @TableField("version")
    private String version;

    @TableField("status")
    private String status;

    @TableField("owner_dept")
    private String ownerDept;

    @TableField("approver")
    private String approver;

    @TableField("description")
    private String description;

    @TableField("content")
    private String content;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    // 状态常量
    public static final String STATUS_DRAFT = "draft";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_DEPRECATED = "deprecated";
    public static final String STATUS_ARCHIVED = "archived";

    // 层级常量
    public static final String LEVEL_3 = "level3";
    public static final String LEVEL_4 = "level4";
    public static final String LEVEL_5 = "level5";
}
