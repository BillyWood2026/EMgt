package com.example.strategymgt.budget.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("budget_dimension")
public class BudgetDimension {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String dimCode;
    private String dimName;
    private String dimType;
    private String dimValue;
    private String parentDimCode;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
