package com.example.datamgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("data_dict_item")
public class DataDictItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String typeCode;
    private String itemValue;
    private String itemLabel;
    private Integer sort;
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
