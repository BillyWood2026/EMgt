package com.example.strategymgt.budget.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("budget_subject")
public class BudgetSubject {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String subjectCode;
    private String subjectName;
    private String subjectType;
    private String parentSubjectCode;
    private Integer level;
    private String legalEntity;
    private String orgLevel;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
