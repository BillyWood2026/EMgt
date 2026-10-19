package com.example.partyaffairsmgt.opinion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 舆情关键词库（需求 8.1：分组、权重）。 */
@Data
@TableName("opinion_keyword")
public class OpinionKeyword {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    /** 分组：企业/品牌/人物/业务 */
    private String groupName;
    /** 权重1-10 */
    private Integer weight;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
