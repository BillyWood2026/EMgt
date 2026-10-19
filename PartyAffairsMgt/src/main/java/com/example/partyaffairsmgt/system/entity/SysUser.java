package com.example.partyaffairsmgt.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 系统用户（需求 12.1）。 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String account;
    private String name;
    private String phone;
    private Long orgId;
    private String orgName;
    private String position;
    /** 角色编码逗号分隔 */
    private String roleCodes;
    /** 启用/停用/锁定 */
    private String status;
    private String pwdHash;
    private LocalDateTime pwdUpdateTime;
    private Integer failCount;
    private LocalDateTime lockUntil;
    private LocalDateTime lastLoginTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
