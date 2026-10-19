package com.example.datamgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("data_asset")
public class DataAsset {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String assetName;
    private String category;
    private String owner;
    private String secretLevel;
    private String lineageDesc;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
