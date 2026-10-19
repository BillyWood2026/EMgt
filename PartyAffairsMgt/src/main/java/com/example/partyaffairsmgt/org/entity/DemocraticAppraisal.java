package com.example.partyaffairsmgt.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("democratic_appraisal")
public class DemocraticAppraisal {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private String orgName;
    private Long meetingId;
    private Long memberId;
    private String memberName;
    private Integer appraisalYear;
    private String selfComment;
    private String mutualComment;
    private String orgOpinion;
    private String result;
    private LocalDate appraisalDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
