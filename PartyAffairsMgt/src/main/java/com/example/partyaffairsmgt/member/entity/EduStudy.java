package com.example.partyaffairsmgt.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("edu_study")
public class EduStudy {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String memberName;
    private Long orgId;
    private String orgName;
    private Long courseId;
    private String courseName;
    private LocalDate studyDate;
    private Integer studyMinutes;
    private Integer courseMinutes;
    private Integer progress;
    private Integer validFlag;
    private BigDecimal scoreHours;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
