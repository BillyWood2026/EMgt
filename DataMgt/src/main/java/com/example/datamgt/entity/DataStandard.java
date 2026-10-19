package com.example.datamgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("data_standard")
public class DataStandard {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String stdCode;
    private String stdName;
    private String category;
    private String dataType;
    private Integer length;
    private String valueRange;
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
