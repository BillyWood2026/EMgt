package com.example.occhealthmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("oh_protection")
public class OhProtection {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 防护设施/用品名称 */
    private String facilityName;
    /** 类别(防护设施/防护用品) */
    private String category;
    /** 适用岗位 */
    private String post;
    /** 配备标准 */
    private String standard;
    /** 数量 */
    private Integer quantity;
    /** 检查周期 */
    private String checkCycle;
    /** 最近检查日期 */
    private LocalDate lastCheckDate;
    /** 状态 */
    private String status;
    /** 备注 */
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
