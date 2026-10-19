package com.example.partyaffairsmgt.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 谈心谈话年度计划与覆盖规则 */
@Data
@TableName("talk_plan")
public class TalkPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private String orgName;
    private Integer planYear;
    private String leaderRule;
    private String committeeRule;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
