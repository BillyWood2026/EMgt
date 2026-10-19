package com.example.partyaffairsmgt.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("floating_member")
public class FloatingMember {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String memberName;
    private Long orgId;
    private String orgName;
    private String flowType;
    private String destination;
    private String contactPhone;
    private LocalDate startDate;
    private LocalDate endDate;
    private String activitySituation;
    private String manager;
    private String status;
    private String certNo;
    private LocalDate lastContactDate;
    private Integer contactCycle;
    private String floatAlertStatus;
    private String flowStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
