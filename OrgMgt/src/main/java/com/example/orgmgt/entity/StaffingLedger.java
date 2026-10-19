package com.example.orgmgt.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("staffing_ledger")
public class StaffingLedger {

    /**
     * Primary Key
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Organization ID
     */
    private Long orgId;

    /**
     * Organization Code
     */
    private String orgCode;

    /**
     * Organization Name
     */
    private String orgName;

    /**
     * Position ID
     */
    private Long positionId;

    /**
     * Position Code
     */
    private String positionCode;

    /**
     * Position Name
     */
    private String positionName;

    /**
     * Position Level
     */
    private String positionLevel;

    /**
     * Staffing Type (FIXED/FLEXIBLE)
     */
    private String staffingType;

    /**
     * Standard Quota
     */
    private Integer standardQuota;

    /**
     * Effective Headcount
     */
    private Integer effectiveCount;

    /**
     * Current Onboard Count
     */
    private Integer currentCount;

    /**
     * Vacant Count
     */
    private Integer vacantCount;

    /**
     * Exceeded Count
     */
    private Integer exceededCount;

    /**
     * Utilization Rate
     */
    private BigDecimal utilizationRate;

    /**
     * Current Management Span
     */
    private Integer managementSpanCurrent;

    /**
     * Standard Management Span
     */
    private Integer managementSpanStandard;

    /**
     * Maximum Management Span
     */
    private Integer managementSpanMax;

    /**
     * Span Status (NORMAL/WARNING/EXCEEDED)
     */
    private String spanStatus;

    /**
     * Warning Level (NORMAL/WARNING/CRITICAL)
     */
    private String warningLevel;

    /**
     * Effective Date
     */
    private LocalDate effectiveDate;

    /**
     * Expire Date
     */
    private LocalDate expireDate;

    /**
     * Remarks
     */
    private String remarks;

    /**
     * Created By
     */
    private String createdBy;

    /**
     * Create Time
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * Updated By
     */
    private String updatedBy;

    /**
     * Update Time
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * Index Number
     */
    @TableField("index_no")
    private String indexNo;

}