package com.example.filemgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_attachment")
public class BizAttachment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String serviceCode;
    private String businessBillId;
    private Long fileId;
    private Integer readOnly;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
