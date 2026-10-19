package com.example.safetymgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("safety_training")
public class SafetyTraining {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 培训名称 */
    private String trainingName;
    /** 培训讲师 */
    private String trainer;
    /** 培训日期 */
    private LocalDate trainDate;
    /** 培训地点 */
    private String location;
    /** 参训人数 */
    private Integer participantCount;
    /** 培训内容 */
    private String content;
    /** 考核结果 */
    private String result;
    /** 状态 */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
