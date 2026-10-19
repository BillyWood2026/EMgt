package com.example.partyaffairsmgt.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("transfer_flow")
public class TransferFlow {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long transferId;
    private String node;
    private String approver;
    private String opinion;
    private LocalDateTime auditTime;
    private String status;
    private LocalDateTime createTime;
}
