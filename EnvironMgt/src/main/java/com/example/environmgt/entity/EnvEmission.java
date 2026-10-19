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
@TableName("env_emission")
public class EnvEmission {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 排口编码 */
    private String outletCode;
    /** 排口名称 */
    private String outletName;
    /** 污染物类型 */
    private String pollutantType;
    /** 许可排放量 */
    private BigDecimal permitValue;
    /** 实际排放量 */
    private BigDecimal actualValue;
    /** 单位 */
    private String unit;
    /** 排放周期 */
    private String dischargePeriod;
    /** 状态 */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
