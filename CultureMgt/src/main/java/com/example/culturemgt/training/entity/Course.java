package com.example.culturemgt.training.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 文化课程元数据（视频/存储复用培训中台，CCMS 维护元数据）。 */
@Data
@TableName("cul_course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String courseName;
    private String courseCategory;
    private String midCourseId;
    private BigDecimal creditHours;
    private String orgScope;
    private Integer requiredFlag;
    private String status;
    private String operator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
