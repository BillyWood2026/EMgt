package com.example.culturemgt.training.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 考试实例。状态机：待考试 → 已完成合格 / 已完成不合格待补考 → 补考完成合格 / 补考仍不合格。
 * 不及格自动冻结评优/晋升/调薪资格（frozen=1），任一考试补考通过自动解冻。
 */
@Data
@TableName("cul_exam_instance")
public class ExamInstance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private String examName;
    private String userName;
    private String userDept;
    private String status;
    private Integer score;
    private Integer attempt;
    private Integer switchCount;
    private String switchLog;
    private String answers;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private Integer frozen;
    private LocalDateTime frozenTime;
    private LocalDateTime unfrozenTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
