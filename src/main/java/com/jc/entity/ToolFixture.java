package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 工装表
 * @TableName T_TOOL_FIXTURE
 */
@TableName(value ="T_TOOL_FIXTURE")
@Schema(description = "工装表实体")
@Data
public class ToolFixture implements Serializable {
    /**
     * 工装id
     */
    @TableId(value = "ID",type = IdType.ASSIGN_UUID)
    @Schema(description = "工装id")
    private String id;

    /**
     * 工装名称
     */
    @TableField(value = "NAME")
    @Schema(description = "工装名称")
    private String name;

    /**
     * 工装类型
     */
    @TableField(value = "TYPE")
    @Schema(description = "工装类型")
    private String type;

    /**
     * 状态
     */
    @TableField(value = "STATUS")
    @Schema(description = "状态")
    private String status;

    /**
     * 生产厂家
     */
    @TableField(value = "MANUFACTURER")
    @Schema(description = "生产厂家")
    private String manufacturer;

    /**
     * 出厂日期
     */
    @TableField(value = "MANUFACTURERDATE")
    @Schema(description = "出厂日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime manufacturerdate;

    /**
     * 使用年限
     */
    @TableField(value = "SERVICELIFE")
    @Schema(description = "使用年限")
    private BigDecimal servicelife;

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

    @TableField(value = "SUBSYSTEM")
    @Schema(description = "所属分系统")
    private String subsystem;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}