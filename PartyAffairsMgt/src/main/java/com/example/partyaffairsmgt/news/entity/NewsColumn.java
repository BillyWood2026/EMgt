package com.example.partyaffairsmgt.news.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("news_column")
public class NewsColumn {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private Long parentId;
    private String icon;
    private Integer sortOrder;
    /** 可见范围：公开/全体党员/内部 */
    private String visible;
    /** 是否允许投稿 */
    private Integer allowContribution;
    /** 发布审批级别：一审/二审/三审 */
    private String approveLevel;
    private String description;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
