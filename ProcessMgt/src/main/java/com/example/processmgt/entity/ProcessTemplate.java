package com.example.processmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程模板 Entity
 */
@Data
@TableName("process_template")
public class ProcessTemplate {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("category")
    private String category;

    @TableField("description")
    private String description;

    @TableField("content")
    private String content;

    @TableField("status")
    private String status;

    @TableField("create_by")
    private String createBy;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    // 状态常量
    public static final String STATUS_DRAFT = "draft";
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_INACTIVE = "inactive";
}
