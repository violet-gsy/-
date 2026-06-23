package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 发射场设备表
 * @TableName T_LS_EQUIPMENT
 */
@TableName(value ="T_LS_EQUIPMENT")
@Schema(description = "发射场设备表实体")
@Data
public class LsEquipment implements Serializable {
    /**
     * 设备id
     */
    @TableId(value = "ID",type = IdType.ASSIGN_UUID)
    @Schema(description = "设备id")
    private String id;

    /**
     * 设备名称
     */
    @TableField(value = "NAME")
    @Schema(description = "设备名称")
    private String name;

    /**
     * 设备类型
     */
    @TableField(value = "TYPE")
    @Schema(description = "设备类型")
    private String type;

    /**
     * 生产厂家
     */
    @TableField(value = "PRODUCER")
    @Schema(description = "生产厂家")
    private String producer;

    /**
     * 设备状态
     */
    @TableField(value = "STATUS")
    @Schema(description = "设备状态")
    private String status;


    /**
     * 出厂日期
     */
    @TableField(value = "MANUFACTURERDATE")
    @Schema(description = "出厂日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime manufacturerdate;

    /**
     * 设计使用年限
     */
    @TableField(value = "SERVICELIFE")
    @Schema(description = "设计使用年限")
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