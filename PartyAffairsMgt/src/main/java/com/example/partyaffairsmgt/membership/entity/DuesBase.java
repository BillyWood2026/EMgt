package com.example.partyaffairsmgt.membership.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 党费计算基数（5.3）。 */
@Data
@TableName("dues_base")
public class DuesBase {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String memberName;
    private Long orgId;
    private String orgName;
    /** 月薪/年薪制/个体/离退休/农民/学生/下岗失业/抚恤救济/低保 */
    private String memberCategory;
    private BigDecimal baseAmount;
    private BigDecimal fixedAmount;
    private BigDecimal ratio;
    private Integer effYear;
    private Integer effMonth;
    private BigDecimal prevBase;
    /** 正常/待审批/已批准 */
    private String auditStatus;
    private String creator;
    private String approver;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
