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
@TableName("energy_consumption")
public class EnergyConsumption {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 统计周期 */
    private String statPeriod;
    /** 能源类型(电/水/天然气/蒸汽) */
    private String energyType;
    /** 消耗量 */
    private BigDecimal consumption;
    /** 峰段消耗 */
    private BigDecimal peakValue;
    /** 谷段消耗 */
    private BigDecimal valleyValue;
    /** 计量单位 */
    private String unit;
    /** 费用(元) */
    private BigDecimal cost;
    /** 使用部门 */
    private String dept;
    /** 厂区 */
    private String factoryArea;
    /** 产线 */
    private String productionLine;
    /** 能源基线 */
    private BigDecimal baseline;
    /** 产量 */
    private Integer output;
    /** 折标煤(吨) */
    private BigDecimal standardCoal;
    /** 备注 */
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
