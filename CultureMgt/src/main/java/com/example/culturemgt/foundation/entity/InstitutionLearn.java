package com.example.culturemgt.foundation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("cul_institution_learn")
public class InstitutionLearn {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long institutionId;
    private String userName;
    private String orgName;
    private LocalDateTime learnTime;
}
