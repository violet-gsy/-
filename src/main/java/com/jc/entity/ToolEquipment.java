package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @TableId(value = "ID",type = IdType.ASSIGN_UUID)
    @Schema(description = "工器具id")
    private String id;


    /**
     * 工器具名称
     */
    @TableField(value = "NAME")
    @Schema(description = "工器具名称")
    private String name;

    /**
     * 工器具类型
     */
    @TableField(value = "TYPE")
    @Schema(description = "工器具类型")
    private String type;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime purchasedate;


    /**
     * 备注
     */
    @TableField(value = "REMARK")
    @Schema(description = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "CREATETIME",fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createtime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}