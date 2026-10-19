package com.example.partyaffairsmgt.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("edu_teacher")
public class EduTeacher {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String teacherName;
    private String title;
    private String specialty;
    private Long orgId;
    private String phone;
    private Integer enabled;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
