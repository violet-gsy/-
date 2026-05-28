package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    @TableId(value = "EQPID",type = IdType.ASSIGN_UUID)
    @Schema(description = "设备id")
    private String eqpid;

    /**
     * 区域id
     */
    @TableField(value = "AREAID")
    @Schema(description = "区域id")
    private String areaid;

    /**
     * 设施id
     */
    @TableField(value = "FACILITYID")
    @Schema(description = "设施id")
    private String facilityid;

    /**
     * 专业ID
     */
    @TableField(value = "MAJORID")
    @Schema(description = "专业ID")
    private String majorid;

    /**
     * 设备名称
     */
    @TableField(value = "EQPNAME")
    @Schema(description = "设备名称")
    private String eqpname;

    /**
     * 设备类型
     */
    @TableField(value = "EQPTYPE")
    @Schema(description = "设备类型")
    private String eqptype;

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
     * 投用日期
     */
    @TableField(value = "INSTALLDATE")
    @Schema(description = "投用日期")
    private LocalDateTime installdate;

    /**
     * 出厂日期
     */
    @TableField(value = "MANUFACTURERDATE")
    @Schema(description = "出厂日期")
    private LocalDateTime manufacturerdate;

    /**
     * 设计使用年限
     */
    @TableField(value = "SERVICELIFE")
    @Schema(description = "设计使用年限")
    private BigDecimal servicelife;

    /**
     * 维保 / 供货单位
     */
    @TableField(value = "SUPPLIER")
    @Schema(description = "维保 / 供货单位")
    private String supplier;

    /**
     * 备注
     */
    @TableField(value = "REMARK")
    @Schema(description = "备注")
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}