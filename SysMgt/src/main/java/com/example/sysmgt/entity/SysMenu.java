package com.example.sysmgt.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("parent_id")
    private Long parentId;

    @TableField("menu_code")
    private String menuCode;

    @TableField("menu_name")
    private String menuName;

    @TableField("menu_type")
    private String menuType;

    @TableField("path")
    private String path;

    @TableField("component")
    private String component;

    @TableField("icon")
    private String icon;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("status")
    private String status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
