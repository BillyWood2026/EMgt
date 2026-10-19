package com.example.strategymgt.dste.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("dste_supervision")
public class DsteSupervision {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String supervisionCode;
    private String supervisionTopic;
    private String supervisionSource;
    private String supervisionLevel;
    private String supervisionTarget;
    private String supervisionContent;
    private LocalDate rectifyDeadline;
    private String initiator;
    private LocalDateTime initiateTime;
    private String rectifyFeedback;
    private String reviewResult;
    private String closeStatus;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
