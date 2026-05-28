package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

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
    @TableId(value = "DOCID",type = IdType.ASSIGN_UUID)
    @Schema(description = "文档ID")
    private String docid;

    /**
     * 文档名称
     */
    @TableField(value = "DOCNAME")
    @Schema(description = "文档名称")
    private String docname;

    /**
     * 文档类型
     */
    @TableField(value = "DOCTYPE")
    @Schema(description = "文档类型")
    private String doctype;

    /**
     * 关联业务类型
     */
    @TableField(value = "BUSINESSTYPE")
    @Schema(description = "关联业务类型")
    private String businesstype;

    /**
     * 关联业务数据ID
     */
    @TableField(value = "BUSINESSID")
    @Schema(description = "关联业务数据ID")
    private String businessid;

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
     * 状态
     */
    @TableField(value = "STATUS")
    @Schema(description = "状态")
    private String status;

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
    @TableField(value = "CREATETIME")
    @Schema(description = "创建时间")
    private LocalDateTime createtime;

    /**
     * 更新时间
     */
    @TableField(value = "UPDATETIME")
    @Schema(description = "更新时间")
    private LocalDateTime updatetime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}