package com.example.improvemgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("improve_result")
public class ImprovementResult {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("result_no")
    private String resultNo;

    @TableField("impl_id")
    private Long implId;

    @TableField("name")
    private String name;

    @TableField("type")
    private String type;

    @TableField("owner")
    private String owner;

    @TableField("actual_savings")
    private BigDecimal actualSavings;

    @TableField("investment_amount")
    private BigDecimal investmentAmount;

    @TableField("roi")
    private BigDecimal roi;

    @TableField("reward_amount")
    private BigDecimal rewardAmount;

    @TableField("reward_status")
    private String rewardStatus;

    @TableField("reward_time")
    private LocalDateTime rewardTime;

    @TableField("complete_date")
    private LocalDate completeDate;

    @TableField("score")
    private Integer score;

    @TableField("status")
    private String status;

    @TableField("description")
    private String description;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_CONFIRMED = "confirmed";
    public static final String STATUS_AWARDED = "awarded";
    public static final String STATUS_ARCHIVED = "archived";

    public static final String REWARD_PENDING = "pending";
    public static final String REWARD_AWARDED = "awarded";
}
