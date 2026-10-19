package com.example.culturemgt.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 文化氛围测评维度与权重（算法61 加权输入）。 */
@Data
@TableName("cul_survey_dimension")
public class SurveyDimension {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String dimensionName;
    private BigDecimal weight;
    private String status;
    private String remark;
    private String operator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
