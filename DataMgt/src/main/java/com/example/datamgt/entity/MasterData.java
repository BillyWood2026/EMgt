package com.example.datamgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("master_data")
public class MasterData {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String dataCode;
    private String dataName;
    private String dataType;
    private String version;
    private String status;
    private LocalDate effectiveDate;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
