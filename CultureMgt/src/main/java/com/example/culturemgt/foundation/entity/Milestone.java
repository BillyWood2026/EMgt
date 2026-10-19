package com.example.culturemgt.foundation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("cul_milestone")
public class Milestone {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate occurDate;
    private String title;
    private String content;
    private String imageUrl;
    private String tags;
    private Integer showFront;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
