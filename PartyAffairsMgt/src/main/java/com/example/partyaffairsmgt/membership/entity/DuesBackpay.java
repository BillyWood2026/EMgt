package com.example.partyaffairsmgt.membership.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 党费补缴登记（5.3）。 */
@Data
@TableName("dues_backpay")
public class DuesBackpay {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String memberName;
    private Long orgId;
    private String orgName;
    private Integer fromYearmonth;
    private Integer toYearmonth;
    private BigDecimal amount;
    private String reason;
    private Integer branchAgreed;
    /** 跨度超 6 个月（E-DU-11） */
    private Integer overSixMonth;
    private String operator;
    private LocalDate payDate;
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
