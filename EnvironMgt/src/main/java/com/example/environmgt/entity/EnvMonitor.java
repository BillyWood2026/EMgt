package com.example.environmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("env_monitor")
public class EnvMonitor {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 监测点位 */
    private String monitorPoint;
    /** 污染物因子 */
    private String pollutant;
    /** 监测值 */
    private BigDecimal monitorValue;
    /** 标准限值 */
    private BigDecimal standardValue;
    /** 单位 */
    private String unit;
    /** 监测时间 */
    private LocalDateTime monitorTime;
    /** 预警级别(轻微/一般/严重) */
    private String warningLevel;
    /** 整改责任人 */
    private String rectifyOwner;
    /** 状态 */
    private String status;
    /** 备注 */
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
