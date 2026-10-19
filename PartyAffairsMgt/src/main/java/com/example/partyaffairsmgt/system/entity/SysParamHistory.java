package com.example.partyaffairsmgt.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 参数变更历史（需求 12.6：支撑一键回滚）。 */
@Data
@TableName("sys_param_history")
public class SysParamHistory {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String paramKey;
    private Integer version;
    private String oldValue;
    private String newValue;
    private String changedBy;
    private LocalDateTime changeTime;
    private String remark;
}
