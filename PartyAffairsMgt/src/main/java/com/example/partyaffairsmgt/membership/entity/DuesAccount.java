package com.example.partyaffairsmgt.membership.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 党费专用账户（5.3，开户银行限六大行 E-DU-09）。 */
@Data
@TableName("dues_account")
public class DuesAccount {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private String orgName;
    private String bankName;
    private String accountNo;
    private String accountName;
    private Integer enabled;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
