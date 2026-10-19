package com.example.iam.entity.system;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_dict")
public class SysDict {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String dictCode;

    private String dictName;

    private String description;

    private Integer sortOrder;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @Version
    private Long version;

    @TableLogic
    private Integer deleted;
}
