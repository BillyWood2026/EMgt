package com.example.partyaffairsmgt.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 应谈名单与覆盖情况 */
@Data
@TableName("talk_coverage")
public class TalkCoverage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    private Long orgId;
    private Integer planYear;
    private String talkerName;
    private Long intervieweeId;
    private String intervieweeName;
    private String requiredRule;
    private LocalDate dueDate;
    private Long heartTalkId;
    private Integer covered;
    private LocalDate coverDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
