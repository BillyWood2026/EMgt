package com.example.partyaffairsmgt.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("heart_talk")
public class HeartTalk {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private String orgName;
    private String talkerName;
    private String talkerIdentity;
    private String intervieweeName;
    private String intervieweeIdentity;
    private String talkType;
    /** 个别谈话 / 集体谈话 */
    private String talkMode;
    private LocalDate talkDate;
    private String location;
    private String topic;
    private String content;
    /** 需要解决的问题 */
    private String issueToSolve;
    private String followerName;
    /** 未跟进/跟进中/已解决 */
    private String followStatus;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
