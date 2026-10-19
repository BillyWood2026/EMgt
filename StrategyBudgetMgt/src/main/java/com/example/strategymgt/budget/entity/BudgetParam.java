package com.example.strategymgt.budget.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("budget_param")
public class BudgetParam {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String paramCode;
    private String paramName;
    private String paramValue;
    private String paramType;
    private String description;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
