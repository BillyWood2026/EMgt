package com.example.culturemgt.behavior.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文化积分流水
 */
@Data
@TableName("cul_behavior_score_txn")
public class BehaviorScoreTxn {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userName;

    /** 4枚举维度 */
    private String dimension;

    /** 加/减 */
    private String direction;

    private Double amount;

    /** 举证/稽核/评议/履职/手动调整 */
    private String sourceType;

    private Long sourceId;

    private String sourceNo;

    /** yyyy-MM */
    private String periodTag;

    /** 经办人 */
    private String operator;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime txnTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
