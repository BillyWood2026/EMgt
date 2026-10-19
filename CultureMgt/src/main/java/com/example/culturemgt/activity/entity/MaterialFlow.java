package com.example.culturemgt.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("cul_material_flow")
public class MaterialFlow {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long materialId;
    private Long activityId;
    private String flowType;
    private Integer qty;
    private Integer balanceAfter;
    private String operator;
    private String reason;
    private LocalDateTime createTime;
    private Integer deleted;
}
