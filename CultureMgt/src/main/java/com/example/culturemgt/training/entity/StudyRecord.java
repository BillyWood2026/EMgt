package com.example.culturemgt.training.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 课程学习记录（中台完成状态回写）。 */
@Data
@TableName("cul_study_record")
public class StudyRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String taskName;
    private Long courseId;
    private String courseName;
    private String userName;
    private String userDept;
    private Integer progress;
    private String status;
    private LocalDateTime finishTime;
    private Integer syncFromMid;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
