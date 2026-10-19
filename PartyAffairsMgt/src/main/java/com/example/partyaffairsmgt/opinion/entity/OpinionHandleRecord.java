package com.example.partyaffairsmgt.opinion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 处置记录（需求 8.4：研判/转办/处置/反馈/复核/归档各环节留痕）。 */
@Data
@TableName("opinion_handle_record")
public class OpinionHandleRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long handleId;
    /** 环节：研判/转办/处置/反馈/复核/归档/升级 */
    private String node;
    private String operator;
    private LocalDateTime recordTime;
    /** 结论/意见 */
    private String conclusion;
    private String remark;
    private LocalDateTime createTime;
}
