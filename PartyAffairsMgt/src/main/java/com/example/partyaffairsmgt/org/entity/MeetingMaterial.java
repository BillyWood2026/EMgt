package com.example.partyaffairsmgt.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 会前准备材料：方案/学习记录/征求意见汇总/谈心谈话记录 */
@Data
@TableName("meeting_material")
public class MeetingMaterial {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long meetingId;
    private String materialType;
    private String fileName;
    private String fileUrl;
    private LocalDateTime uploadTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
