package com.example.partyaffairsmgt.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 系统角色（需求 12.1：页面/按钮/字段/数据范围四级权限）。 */
@Data
@TableName("sys_role")
public class SysRole {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String roleCode;
    private String roleName;
    private String pagePerms;
    private String buttonPerms;
    private String fieldPerms;
    /** 全部/本组织/本人 */
    private String dataScope;
    private String remark;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
