package com.example.partyaffairsmgt.membership.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 发展党员对象主档（5.2 五阶段）。 */
@Data
@TableName("dev_person")
public class DevPerson {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private String ethnicity;
    private String education;
    private String department;
    private String position;
    private String phone;
    private String idCard;
    private Long orgId;
    private String orgName;
    /** APPLICANT / ACTIVIST / TARGET / PROBATION / FORMAL */
    private String stage;
    /** 在途/已发展/已取消/已延长/已推迟 */
    private String status;
    private LocalDate leaveDate;
    private Integer extendMonths;
    private LocalDate probationStart;
    private LocalDate probationEnd;
    private String currentNode;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
