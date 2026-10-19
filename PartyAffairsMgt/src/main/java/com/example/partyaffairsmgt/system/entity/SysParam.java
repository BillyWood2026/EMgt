package com.example.partyaffairsmgt.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 参数配置（需求 12.6：管理员修改+审计+一键回滚）。 */
@Data
@TableName("sys_param")
public class SysParam {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String paramKey;
    private String paramName;
    private String paramValue;
    /** 文本/数值/布尔/JSON */
    private String valueType;
    private String category;
    private Integer version;
    private String updatedBy;
    private LocalDateTime updateTime;
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
