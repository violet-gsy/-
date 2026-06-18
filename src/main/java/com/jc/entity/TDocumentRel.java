package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文档关联表
 * @TableName T_DOCUMENT_REL
 */
@TableName(value ="T_DOCUMENT_REL")
@Schema(description = "文档关联表实体")
@Data
public class TDocumentRel implements Serializable {
    /**
     * 关联ID
     */
    @TableId(value = "RELID",type = IdType.ASSIGN_UUID)
    @Schema(description = "关联ID")
    private String relid;

    /**
     * 文档ID
     */
    @TableField(value = "DOCID")
    @Schema(description = "文档ID")
    private String docid;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}