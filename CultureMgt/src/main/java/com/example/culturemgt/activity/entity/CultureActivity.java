package com.example.culturemgt.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("cul_activity")
public class CultureActivity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String actName;
    private String actType;
    private String organizerDept;
    private String manager;
    private LocalDateTime planStart;
    private LocalDateTime planEnd;
    private LocalDateTime signDeadline;
    private Integer maxSign;
    private Integer shouldCount;
    private Integer signedCount;
    private Integer checkedCount;
    private Long placeId;
    private String intro;
    private String archiveFiles;
    private String summary;
    private String resultEval;
    private String status;
    private Integer stage;
    private String approvalLog;
    private String rejectReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
