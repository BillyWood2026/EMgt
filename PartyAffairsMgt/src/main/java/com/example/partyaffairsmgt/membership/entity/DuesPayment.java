package com.example.partyaffairsmgt.membership.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("dues_payment")
public class DuesPayment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String memberName;
    private Long orgId;
    private String orgName;
    private Integer feeYear;
    private Integer feeMonth;
    private BigDecimal baseAmount;
    private BigDecimal dueAmount;
    private BigDecimal paidAmount;
    private LocalDate payDate;
    private String payType;
    private String collector;
    private String status;
    private String billNo;
    /** 月薪/年薪制/个体/离退休/农民/学生/下岗失业/抚恤救济/低保 */
    private String memberCategory;
    private BigDecimal ratio;
    private String voucherNo;
    private String confirmer;
    private LocalDateTime confirmTime;
    private Long reduceId;
    private Long backpayId;
    private String diffReason;
    private Integer largeFlag;
    private Long remitId;
    /** 现金/转账/银行代扣/线上缴纳/委托代缴 */
    private String payMethod;
    private String collectorName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
