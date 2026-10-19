package com.example.partyaffairsmgt.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("score_flow")
public class ScoreFlow {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String memberName;
    private Long orgId;
    private String orgName;
    private String ruleKey;
    private String ruleName;
    private Integer score;
    private String sourceType;
    private Long sourceId;
    private String auditStatus;
    private String approver;
    private LocalDateTime auditTime;
    private Integer scoreYear;
    private Integer scoreMonth;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
