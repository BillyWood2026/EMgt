package com.example.culturemgt.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("cul_material")
public class CultureMaterial {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String matName;
    private String matType;
    private String spec;
    private String unit;
    private Integer stock;
    private Integer safetyStock;
    private String keeper;
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;

    @TableField(exist = false)
    private Boolean lowStock;
}
