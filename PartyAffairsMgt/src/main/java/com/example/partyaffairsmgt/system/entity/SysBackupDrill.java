package com.example.partyaffairsmgt.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 备份恢复演练（需求 12.7：RPO<=15分钟 RTO<=120分钟）。 */
@Data
@TableName("sys_backup_drill")
public class SysBackupDrill {

    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate drillDate;
    private String operator;
    private Integer rpoMinutes;
    private Integer rtoMinutes;
    /** 达标/不达标 */
    private String result;
    private String notes;
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
