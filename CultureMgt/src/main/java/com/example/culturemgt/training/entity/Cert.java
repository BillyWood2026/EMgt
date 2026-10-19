package com.example.culturemgt.training.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 文化认证证书（年度文化合格认证/优秀文化践行者认证，编号 CT+年份+4位）。 */
@Data
@TableName("cul_cert")
public class Cert {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String certNo;
    private String userName;
    private String userDept;
    private String certType;
    private Long sourceExamId;
    private String reason;
    private LocalDate issueDate;
    private String issuer;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
