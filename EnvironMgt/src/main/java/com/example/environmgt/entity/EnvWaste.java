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
@TableName("env_waste")
public class EnvWaste {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 废弃物编码 */
    private String wasteCode;
    /** 废弃物名称 */
    private String wasteName;
    /** 类别(危险废物/一般固废) */
    private String category;
    /** 数量 */
    private BigDecimal quantity;
    /** 单位 */
    private String unit;
    /** 处置方式 */
    private String disposeMethod;
    /** 处置单位 */
    private String vendor;
    /** 产生日期 */
    private LocalDate produceDate;
    /** 状态 */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
