package com.example.discipline.discipline.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("discipline_case")
public class DisciplineCase {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String caseNo;
    private String caseType;
    private String caseLevel;
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
