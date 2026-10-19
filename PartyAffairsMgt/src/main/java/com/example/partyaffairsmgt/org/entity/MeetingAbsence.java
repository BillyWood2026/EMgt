package com.example.partyaffairsmgt.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 缺席人员及原因（原因必填） */
@Data
@TableName("meeting_absence")
public class MeetingAbsence {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long meetingId;
    private Long memberId;
    private String memberName;
    private String reason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
