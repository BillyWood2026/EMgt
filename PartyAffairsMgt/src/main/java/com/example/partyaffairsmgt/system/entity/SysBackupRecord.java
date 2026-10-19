package com.example.partyaffairsmgt.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 数据备份记录（需求 12.7）。 */
@Data
@TableName("sys_backup_record")
public class SysBackupRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 增量/全量 */
    private String backupType;
    private String fileName;
    /** 字节 */
    private Long fileSize;
    private String location;
    /** 成功/失败 */
    private String status;
    private String operator;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String remark;
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
