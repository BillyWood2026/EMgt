package com.example.hrplanning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 培养标准（/api/training-standards，training_standard）
 */
@Data
@TableName("training_standard")
public class TrainingStandard {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("standard_code")
    private String standardCode;

    @TableField("standard_name")
    private String standardName;

    /** 标准类别：课程标准/实践标准/结业标准 */
    @TableField("category")
    private String category;

    @TableField("description")
    private String description;

    @TableField("status")
    private String status;

    @TableField("deleted")
    @TableLogic
    private Integer deleted = 0;

    @TableField("version")
    @Version
    private Integer version = 1;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("update_by")
    private String updateBy;
}
