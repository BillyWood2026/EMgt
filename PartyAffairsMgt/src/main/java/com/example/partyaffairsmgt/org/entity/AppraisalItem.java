package com.example.partyaffairsmgt.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 党员民主评议明细 */
@Data
@TableName("appraisal_item")
public class AppraisalItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    private Long orgId;
    private Long memberId;
    private String memberName;
    /** 是否预备党员（不定等次） */
    private Integer isProbationary;
    private Integer participated;
    private String selfContent;
    /** 四维测评得分：政治表现/遵规守纪/发挥作用/道德品行 */
    private BigDecimal scorePolitics;
    private BigDecimal scoreDiscipline;
    private BigDecimal scoreRole;
    private BigDecimal scoreMorality;
    private BigDecimal mutualScore;
    private BigDecimal weightedScore;
    /** 待自评/已自评/已互评/已支评/已审定/已反馈 */
    private String stage;
    private String branchOpinion;
    /** 优秀/合格/基本合格/不合格/不定等次 */
    private String result;
    private String partyOpinion;
    /** 等次调整理由（必填） */
    private String adjustReason;
    /** 待审批/已批准/已驳回 */
    private String adjustStatus;
    private LocalDate feedbackDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
