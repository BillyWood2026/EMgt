package com.example.attendance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公共实体基类：自增主键、逻辑删除、乐观锁、审计字段
 */
@Data
public abstract class BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("update_by")
    private String updateBy;

    @TableLogic
    @TableField("deleted")
    private Integer deleted = 0;

    @Version
    @TableField("version")
    private Integer version = 1;
}
