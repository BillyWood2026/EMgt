package com.example.partyaffairsmgt.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("score_rule")
public class ScoreRule {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleKey;
    private String ruleName;
    private Integer scoreValue;
    private String period;
    private Integer autoFlag;
    private Integer enabled;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
