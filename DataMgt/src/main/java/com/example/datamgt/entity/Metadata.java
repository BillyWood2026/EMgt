package com.example.datamgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("metadata")
public class Metadata {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String tableName;
    private String fieldName;
    private String cnName;
    private String fieldType;
    private String dbName;
    private String owner;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
