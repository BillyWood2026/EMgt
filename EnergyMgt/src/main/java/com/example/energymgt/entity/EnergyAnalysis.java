package com.example.energymgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("energy_analysis")
public class EnergyAnalysis {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 分析周期 */
    private String analysisPeriod;
    /** 能源类型 */
    private String energyType;
    /** 消耗量 */
    private BigDecimal consumption;
    /** 同比(%) */
    private BigDecimal yoyRate;
    /** 环比(%) */
    private BigDecimal momRate;
    /** 单位产值能耗 */
    private BigDecimal unitOutputConsumption;
    /** 目标值 */
    private BigDecimal targetValue;
    /** 分析结论 */
    private String conclusion;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
