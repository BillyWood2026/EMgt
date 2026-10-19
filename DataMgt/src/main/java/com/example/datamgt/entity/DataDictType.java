package com.example.datamgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("data_dict_type")
public class DataDictType {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String typeCode;
    private String typeName;
    private Integer sort;
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
