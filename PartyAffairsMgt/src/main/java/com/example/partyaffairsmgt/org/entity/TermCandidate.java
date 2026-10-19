package com.example.partyaffairsmgt.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 换届候选人及得票 */
@Data
@TableName("term_candidate")
public class TermCandidate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long electionId;
    private Long memberId;
    private String memberName;
    private Integer partyAgeYears;
    private Integer votes;
    private Integer elected;
    /** 书记/副书记/委员 */
    private String position;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
