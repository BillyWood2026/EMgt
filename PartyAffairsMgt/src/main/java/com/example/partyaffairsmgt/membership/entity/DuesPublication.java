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
@TableName("dues_publication")
public class DuesPublication {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private String orgName;
    private String period;
    private Integer feeYear;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String publicScope;
    private String attachment;
    private BigDecimal totalCollected;
    private BigDecimal totalTurnedOver;
    private BigDecimal totalUsed;
    private BigDecimal balance;
    private String publisher;
    private LocalDate publishDate;
    private String content;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
