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
 * 发射场流体介质表
 * @TableName T_LAUNCH_SITE_FLUID
 */
@TableName(value ="T_LAUNCH_SITE_FLUID")
@Data
public class LaunchSiteFluid implements Serializable {
    /**
     * 介质ID
     */
    @TableId(value = "ID")
    private String id;

    /**
     * 计量单位ID
     */
    @TableField(value = "UNITID")
    private String unitid;

    /**
     * 介质编码
     */
    @TableField(value = "FLUIDCODE")
    private String fluidcode;

    /**
     * 介质名称
     */
    @TableField(value = "FLUIDNAME")
    private String fluidname;

    /**
     * 介质类型
     */
    @TableField(value = "FLUIDTYPE")
    private String fluidtype;

    /**
     * 相态
     */
    @TableField(value = "PHASE")
    private String phase;

    /**
     * 密度
     */
    @TableField(value = "DENSITY")
    private BigDecimal density;

    /**
     * 额定压力
     */
    @TableField(value = "PRESSURERATING")
    private BigDecimal pressurerating;

    /**
     * 标准温度
     */
    @TableField(value = "TEMPERATURE")
    private BigDecimal temperature;

    /**
     * 危险等级
     */
    @TableField(value = "HAZARDLEVEL")
    private String hazardlevel;

    /**
     * 是否助燃
     */
    @TableField(value = "ISSUPPORT")
    private Integer issupport;

    /**
     * 是否有毒
     */
    @TableField(value = "ISTOXIC")
    private Integer istoxic;

    /**
     * 存储方式
     */
    @TableField(value = "STORAGEMODE")
    private String storagemode;

    /**
     * 使用场景
     */
    @TableField(value = "USESCENE")
    private String usescene;

    /**
     * 备注
     */
    @TableField(value = "REMARK")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "CREATETIME")
    private LocalDateTime createtime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}