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
@TableName("energy_saving")
public class EnergySaving {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 项目名称 */
    private String projectName;
    /** 项目编号 */
    private String projectNo;
    /** 节能措施 */
    private String measure;
    /** 投资额(元) */
    private BigDecimal investAmount;
    /** 年节约金额(元) */
    private BigDecimal annualSaving;
    /** 投资回收期(年) */
    private BigDecimal paybackPeriod;
    /** 开始日期 */
    private LocalDate startDate;
    /** 结束日期 */
    private LocalDate endDate;
    /** 责任人 */
    private String responsible;
    /** 状态 */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
