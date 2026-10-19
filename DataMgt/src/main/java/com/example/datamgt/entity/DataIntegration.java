package com.example.datamgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("data_integration")
public class DataIntegration {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskName;
    private String sourceDb;
    private String targetDb;
    private String syncMode;
    private String scheduleCycle;
    private String lastExecStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
