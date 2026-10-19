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
@TableName("edu_plan")
public class EduPlan {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String planName;
    private Long orgId;
    private Integer trainYear;
    private Integer targetCount;
    private String form;
    private BigDecimal targetHours;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String creator;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
