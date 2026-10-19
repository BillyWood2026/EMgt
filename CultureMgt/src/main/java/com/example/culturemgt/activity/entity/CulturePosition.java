package com.example.culturemgt.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("cul_position")
public class CulturePosition {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String posName;
    private String posType;
    private String ownerDept;
    private String manager;
    private Integer updateCycleDays;
    private LocalDate lastUpdateDate;
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;

    @TableField(exist = false)
    private Integer overdueDays;
    @TableField(exist = false)
    private Boolean overdue;
}
