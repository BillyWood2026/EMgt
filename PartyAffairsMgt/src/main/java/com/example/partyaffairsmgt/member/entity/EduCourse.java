package com.example.partyaffairsmgt.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("edu_course")
public class EduCourse {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String courseName;
    private String category;
    private BigDecimal hours;
    private String lecturer;
    private String content;
    private Integer enabled;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
