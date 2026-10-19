package com.example.partyaffairsmgt.assessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("assess_indicator")
public class AssessIndicator {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String indicatorCode;
    private String indicatorName;
    private Integer level;
    private String parentCode;
    private String targetType;
    private BigDecimal score;
    private BigDecimal weight;
    private String scoreMode;
    private String dataSource;
    private String scoringStandard;
    private Integer vetoFlag;
    private String status;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
