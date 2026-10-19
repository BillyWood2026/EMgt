package com.example.filemgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("file_record")
public class FileRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String fileName;
    private String fileExt;
    private Long fileSize;
    private String mimeType;
    private String storagePath;
    private String md5;
    private String uploader;
    private String serviceCode;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
