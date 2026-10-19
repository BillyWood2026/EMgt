package com.example.discipline.supervision.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("supervision_record")
public class SupervisionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String recordNo;
    private String supervisionType;
    private String riskLevel;
    private String status;
    private String responsiblePerson;
    private LocalDateTime occurTime;
    private String handleResult;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
