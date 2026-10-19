package com.example.culturemgt.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文化资产库
 */
@Data
@TableName("cul_culture_asset")
public class CultureAsset {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 编号 ZC+yyyyMMdd+4位 */
    private String assetCode;

    /** 资产名称 */
    private String assetName;

    /** 海报/宣传片/PPT模板/文化手册/文创物料/其他 */
    private String assetType;

    /** 标签 JSON数组 */
    private String tags;

    /** 附件URL */
    private String fileUrl;

    /** 文件大小bytes */
    private Long fileSize;

    /** 可下载组织 JSON数组 */
    private String orgScope;

    /** 下载次数 */
    private Integer downloadCount;

    /** 浏览次数 */
    private Integer viewCount;

    /** 启用/停用 */
    private String status;

    private String creator;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
