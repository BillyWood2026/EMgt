package com.example.datamgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("data_service")
public class DataService {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String serviceCode;
    private String serviceName;
    private String servicePath;
    private String method;
    private String status;
    private Long invokeCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
