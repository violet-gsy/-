package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 发射场设备表
 * @TableName T_LS_EQUIPMENT
 */
@TableName(value ="T_LS_EQUIPMENT")
@Data
public class LsEquipment implements Serializable {
    /**
     * 设备id
     */
    @TableId(value = "EQPID")
    private String eqpid;

    /**
     * 区域id
     */
    @TableField(value = "AREAID")
    private String areaid;

    /**
     * 设施id
     */
    @TableField(value = "FACILITYID")
    private String facilityid;

    /**
     * 专业ID
     */
    @TableField(value = "MAJORID")
    private String majorid;

    /**
     * 设备名称
     */
    @TableField(value = "EQPNAME")
    private String eqpname;

    /**
     * 设备类型
     */
    @TableField(value = "EQPTYPE")
    private String eqptype;

    /**
     * 生产厂家
     */
    @TableField(value = "PRODUCER")
    private String producer;

    /**
     * 设备状态
     */
    @TableField(value = "STATUS")
    private String status;

    /**
     * 投用日期
     */
    @TableField(value = "INSTALLDATE")
    private LocalDateTime installdate;

    /**
     * 出厂日期
     */
    @TableField(value = "MANUFACTURERDATE")
    private LocalDateTime manufacturerdate;

    /**
     * 设计使用年限
     */
    @TableField(value = "SERVICELIFE")
    private BigDecimal servicelife;

    /**
     * 维保 / 供货单位
     */
    @TableField(value = "SUPPLIER")
    private String supplier;

    /**
     * 备注
     */
    @TableField(value = "REMARK")
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}