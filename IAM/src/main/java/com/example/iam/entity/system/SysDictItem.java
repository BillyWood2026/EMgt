package com.example.iam.entity.system;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_dict_item")
public class SysDictItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long dictId;

    private String dictCode;

    private String itemLabel;

    private String itemValue;

    private Integer sortOrder;

    private String status;

    private String remark;

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
