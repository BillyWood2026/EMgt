package com.example.partyaffairsmgt.membership.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("member_development")
public class MemberDevelopment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String applicantName;
    private String gender;
    private String idCard;
    private Long branchId;
    private String branchName;
    private String stage;
    private LocalDate applyDate;
    private LocalDate activistDate;
    private LocalDate targetDate;
    private LocalDate probationDate;
    private LocalDate fullDate;
    private String introducer;
    private String contactPhone;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
