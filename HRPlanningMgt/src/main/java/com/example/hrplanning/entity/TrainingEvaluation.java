package com.example.hrplanning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 培养效果评价（/api/training-evaluations，training_evaluation）
 */
@Data
@TableName("training_evaluation")
public class TrainingEvaluation {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("project_id")
    private Long projectId;

    @TableField("student_name")
    private String studentName;

    @TableField("student_no")
    private String studentNo;

    /** 理论评分 */
    @TableField("theory_score")
    private BigDecimal theoryScore;

    /** 实践评分 */
    @TableField("practice_score")
    private BigDecimal practiceScore;

    /** 综合评分 */
    @TableField("total_score")
    private BigDecimal totalScore;

    /** 评价等级 优秀/合格/不合格 */
    @TableField("evaluation_level")
    private String evaluationLevel;

    @TableField("evaluator")
    private String evaluator;

    @TableField("status")
    private String status;

    @TableField("comment")
    private String comment;

    @TableField("deleted")
    @TableLogic
    private Integer deleted = 0;

    @TableField("version")
    @Version
    private Integer version = 1;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("update_by")
    private String updateBy;
}
