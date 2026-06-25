package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文档历史版本表
 * @TableName T_DOCUMENT_HIS
 */
@TableName(value ="T_DOCUMENT_HIS")
@Schema(description = "文档历史版本表实体")
@Data
public class DocumentHis implements Serializable {
    /**
     * 历史文档id
     */
    @TableId(value = "ID",type = IdType.ASSIGN_UUID)
    @Schema(description = "历史文档id")
    private String id;

    /**
     * 文档ID
     */
    @TableField(value = "DOCID")
    @Schema(description = "文档ID")
    private String docid;

    /**
     * 文档名称
     */
    @TableField(value = "HISDOCNAME")
    @Schema(description = "文档名称")
    private String hisdocname;

    /**
     * 版本号
     */
    @TableField(value = "VERSION")
    @Schema(description = "版本号")
    private String version;

    /**
     * 文件存储路径
     */
    @TableField(value = "HISFILEPATH")
    @Schema(description = "文件存储路径")
    private String hisfilepath;

    /**
     * 文件大小（字节）
     */
    @TableField(value = "HISFILESIZE")
    @Schema(description = "文件大小")
    private Long hisfilesize;

    /**
     * 版本操作人
     */
    @TableField(value = "OPERATOR")
    @Schema(description = "版本操作人")
    private String operator;

    /**
     * 操作类型
     */
    @TableField(value = "OPERATE_TYPE")
    @Schema(description = "操作类型")
    private String operateType;

    /**
     * 版本说明
     */
    @TableField(value = "VERSIONREMARK")
    @Schema(description = "版本说明")
    private String versionremark;

    /**
     * 版本生成时间
     */
    @TableField(value = "CREATETIME",fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createtime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}