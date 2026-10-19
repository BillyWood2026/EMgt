package com.example.filemgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("file_log")
public class FileLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fileId;
    private String opType;
    private String operator;
    private String ip;
    private LocalDateTime opTime;
    private String detail;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
