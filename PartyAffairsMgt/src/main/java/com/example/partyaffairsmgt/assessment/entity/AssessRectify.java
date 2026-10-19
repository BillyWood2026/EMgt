package com.example.partyaffairsmgt.assessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("assess_rectify")
public class AssessRectify {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String problem;
    private String dutyPerson;
    private LocalDate deadline;
    private String status;
    private String finishProof;
    private LocalDateTime closeTime;
    private String closeBy;
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
