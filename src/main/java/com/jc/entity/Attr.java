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
 * 
 * @TableName 属性表
 */
@TableName(value ="T_ATTR")
@Schema(description = "属性表")
@Data
public class Attr implements Serializable {
    /**
     * ID
     */
    @TableId(value = "ID",type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    /**
     * 属性ID
     */
    @TableField(value = "ATTRID")
    @Schema(description = "属性ID(指的是各业务表id)")
    private String attrid;

    @TableField(value = "ATTRKEY")
    @Schema(description = "属性KEY")
    private String attrkey;

    /**
     * 属性名称
     */
    @TableField(value = "ATTRNAME")
    @Schema(description = "属性名称")
    private String attrname;

    /**
     * 属性值
     */
    @TableField(value = "ATTRVALUE")
    @Schema(description = "属性值")
    private String attrvalue;

    /**
     * 属性数据类型id
     */
    @TableField(value = "DATATYPEID")
    @Schema(description = "属性数据类型id")
    private String datatypeid;

    /**
     * 计量单位ID
     */
    @TableField(value = "UNITID")
    @Schema(description = "计量单位ID")
    private String unitid;

    @TableField(value = "CREATETIME")
    @Schema(description = "创建时间")
    private LocalDateTime createtime;

    @TableField(exist = false)
    @Schema(description = "属性数据类型名称")
    private String datatypename;

    @TableField(exist = false)
    @Schema(description = "计量单位名称")
    private String unitname;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}