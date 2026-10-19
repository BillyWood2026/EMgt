package com.example.filemgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("file_version")
public class FileVersion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fileId;
    private String versionNo;
    private Long fileSize;
    private String storagePath;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
