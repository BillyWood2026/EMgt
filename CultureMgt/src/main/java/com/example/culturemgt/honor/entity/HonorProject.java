package com.example.culturemgt.honor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("cul_honor_project")
public class HonorProject {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String projectName;
    private String periodType;
    private String applyType;
    private String orgScope;
    private LocalDate applyStart;
    private LocalDate applyEnd;
    private Integer autoSyncCase;
    private String status;
    private String operator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
