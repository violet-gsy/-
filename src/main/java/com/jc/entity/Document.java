package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文档主表
 * @TableName T_DOCUMENT
 */
@TableName(value ="T_DOCUMENT")
@Schema(description = "文档主表实体")
@Data
public class Document implements Serializable {
    /**
     * 文档ID
     */
    @TableId(value = "ID")
    @Schema(description = "文档ID")
    private String id;

    /**
     * 文档名称
     */
    @TableField(value = "NAME")
    @Schema(description = "文档名称")
    private String name;

    /**
     * 文档类型
     */
    @TableField(value = "TYPE")
    @Schema(description = "文档类型")
    private String type;


    /**
     * 文件存储路径
     */
    @TableField(value = "FILEPATH")
    @Schema(description = "文件存储路径")
    private String filepath;

    /**
     * 文件大小（字节）
     */
    @TableField(value = "FILESIZE")
    @Schema(description = "文件大小")
    private Long filesize;

    /**
     * 当前版本号
     */
    @TableField(value = "VERSION")
    @Schema(description = "当前版本号")
    private String version;

    /**
     * 备注
     */
    @TableField(value = "REMARK")
    @Schema(description = "备注")
    private String remark;

    /**
     * 创建人
     */
    @TableField(value = "CREATOR")
    @Schema(description = "创建人")
    private String creator;

    /**
     * 创建时间
     */
    @TableField(value = "CREATETIME",fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createtime;

    /**
     * 更新时间
     */
    @TableField(value = "UPDATETIME")
    @Schema(description = "更新时间")
    private LocalDateTime updatetime;

    @TableField(value = "SUBSYSTEM")
    @Schema(description = "所属分系统")
    private String subsystem;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}