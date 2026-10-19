package com.example.partyaffairsmgt.membership.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 党费上缴单：年度 5% 上缴 / 大额党费（≥1000）逐级上缴（5.3）。 */
@Data
@TableName("dues_remittance")
public class DuesRemittance {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String remitNo;
    /** 年度上缴/大额党费 */
    private String remitType;
    private Long orgId;
    private String orgName;
    private Integer feeYear;
    private BigDecimal amount;
    private BigDecimal ratio;
    private LocalDate remitDate;
    private String payee;
    private String receiptNo;
    private LocalDate receiptDate;
    /** 待汇出/已汇出/已回收据 */
    private String status;
    private Long relatedPaymentId;
    private String remark;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
