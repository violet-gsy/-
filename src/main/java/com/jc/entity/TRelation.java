package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 表关系关联表
 * @TableName T_RELATION
 */
@TableName(value ="T_RELATION")
@Schema(description = "表关系关联表实体")
@Data
public class TRelation implements Serializable {
    /**
     * 关联ID
     */
    @TableField(value = "RELATIONID")
    @Schema(description = "关联ID")
    private String relationid;

    /**
     * 我方ID
     */
    @TableField(value = "OURID")
    @Schema(description = "我方ID")
    private String ourid;

    /**
     * 我方业务类型
     */
    @TableField(value = "OURBSTYPE")
    @Schema(description = "我方业务类型")
    private String ourbstype;

    /**
     * 目标ID
     */
    @TableField(value = "TARGETID")
    @Schema(description = "目标ID")
    private String targetid;

    /**
     * 目标业务类型
     */
    @TableField(value = "TARGETBSTYPE")
    @Schema(description = "目标业务类型")
    private String targetbstype;

    @TableField(exist = false)
    @Schema(description = "目标名称")
    private String targetname;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}