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
@TableName("education_training")
public class EducationTraining {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String trainingType;
    private Long orgId;
    private String orgName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private String lecturer;
    private BigDecimal hours;
    private String participants;
    private String content;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
