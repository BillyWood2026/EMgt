package com.example.culturemgt.casehub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("cul_case_tag")
public class CaseTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String tagName;
    private Integer scorePositive;
    private Integer scoreNegative;
    private Integer scoreNews;
    private Integer scoreTraining;
    private String status;
    private String remark;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
