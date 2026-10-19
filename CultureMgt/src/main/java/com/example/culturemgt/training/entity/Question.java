package com.example.culturemgt.training.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 文化考试题库（单选/多选/判断，支持Excel批量导入）。 */
@Data
@TableName("cul_question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String questionType;
    private String stem;
    private String optionItems;
    private String answer;
    private String valueTag;
    private String redlineRef;
    private String analysis;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
