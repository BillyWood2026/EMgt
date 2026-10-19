package com.example.partyaffairsmgt.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("member_archive_material")
public class MemberArchiveMaterial {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String materialType;
    private String materialName;
    private String filePath;
    private Integer fileSize;
    private String uploadBy;
    private LocalDateTime uploadTime;
    private String borrower;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
