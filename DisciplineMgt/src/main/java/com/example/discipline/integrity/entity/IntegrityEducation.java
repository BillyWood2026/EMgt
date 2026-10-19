package com.example.discipline.integrity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("integrity_education")
public class IntegrityEducation {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String eduNo;
    private String eduType;
    private String level;
    private String status;
    private String responsiblePerson;
    private LocalDateTime occurTime;
    private String result;
    private Integer participantCount;
    private Integer commitmentCount;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
