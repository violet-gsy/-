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
 * 工器具表
 * @TableName T_TOOL_EQUIPMENT
 */
@TableName(value ="T_TOOL_EQUIPMENT")
@Schema(description = "工器具表实体")
@Data
public class ToolEquipment implements Serializable {
    /**
     * 工器具id
     */
    @TableId(value = "TOOLEQPID",type = IdType.ASSIGN_UUID)
    @Schema(description = "工器具id")
    private String tooleqpid;

    /**
     * 专业ID
     */
    @TableField(value = "MAJORID")
    @Schema(description = "专业ID")
    private String majorid;

    /**
     * 工器具名称
     */
    @TableField(value = "TOOLNAME")
    @Schema(description = "工器具名称")
    private String toolname;

    /**
     * 工器具类型
     */
    @TableField(value = "TOOLTYPE")
    @Schema(description = "工器具类型")
    private String tooltype;

    /**
     * 状态
     */
    @TableField(value = "STATUS")
    @Schema(description = "状态")
    private String status;

    /**
     * 购置日期
     */
    @TableField(value = "PURCHASEDATE")
    @Schema(description = "购置日期")
    private LocalDateTime purchasedate;

    /**
     * 存放位置
     */
    @TableField(value = "LOCATION")
    @Schema(description = "存放位置")
    private String location;

    /**
     * 备注
     */
    @TableField(value = "REMARK")
    @Schema(description = "备注")
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}