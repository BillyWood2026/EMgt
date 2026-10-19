package com.example.partyaffairsmgt.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 监票人/计票人（须非候选人） */
@Data
@TableName("term_witness")
public class TermWitness {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long electionId;
    private String memberName;
    /** 监票人/计票人 */
    private String witnessType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
