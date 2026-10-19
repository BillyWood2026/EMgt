package com.example.culturemgt.training.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 考试任务（自动组卷/自动阅卷/防切屏/补考次数）。 */
@Data
@TableName("cul_exam")
public class Exam {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String examName;
    private String bankIds;
    private Integer durationMin;
    private Integer passLine;
    private Integer maxRetake;
    private Integer antiSwitch;
    private String orgScope;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String operator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
