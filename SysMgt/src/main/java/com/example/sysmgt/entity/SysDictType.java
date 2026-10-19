package com.example.sysmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_dict_type")
public class SysDictType {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("dict_name")
    private String dictName;

    @TableField("dict_type")
    private String dictType;

    /** 前端 Dict.vue 使用的字段名，与 dictType 同值（不入库） */
    @TableField(exist = false)
    private String dictCode;

    /** 前端 Dict.vue 使用的字段名，映射到 remark（不入库） */
    @TableField(exist = false)
    private String description;

    @TableField("status")
    private String status;

    @TableField("remark")
    private String remark;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
