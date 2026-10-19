package com.example.datamgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("data_quality")
public class DataQuality {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleName;
    private String targetObject;
    private String checkType;
    private BigDecimal thresholdValue;
    private String lastCheckResult;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
