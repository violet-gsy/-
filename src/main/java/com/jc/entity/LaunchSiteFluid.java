package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 发射场流体介质表
 * @TableName T_LAUNCH_SITE_FLUID
 */
@TableName(value ="T_LAUNCH_SITE_FLUID")
@Schema(description = "发射场流体介质表实体")
@Data
public class LaunchSiteFluid implements Serializable {
    /**
     * 介质ID
     */
    @TableId(value = "ID",type = IdType.ASSIGN_UUID)
    @Schema(description = "介质ID")
    private String id;


    /**
     * 介质名称
     */
    @TableField(value = "NAME")
    @Schema(description = "介质名称")
    private String name;

    /**
     * 介质类型
     */
    @TableField(value = "TYPE")
    @Schema(description = "介质类型")
    private String type;

    /**
     * 相态
     */
    @TableField(value = "PHASE")
    @Schema(description = "相态")
    private String phase;

    /**
     * 密度
     */
    @TableField(value = "DENSITY")
    @Schema(description = "密度")
    private BigDecimal density;

    /**
     * 额定压力
     */
    @TableField(value = "PRESSURERATING")
    @Schema(description = "额定压力")
    private BigDecimal pressurerating;

    /**
     * 标准温度
     */
    @TableField(value = "TEMPERATURE")
    @Schema(description = "标准温度")
    private BigDecimal temperature;

    /**
     * 危险等级
     */
    @TableField(value = "HAZARDLEVEL")
    @Schema(description = "危险等级")
    private String hazardlevel;

    /**
     * 是否助燃
     */
    @TableField(value = "ISSUPPORT")
    @Schema(description = "是否助燃")
    private String issupport;

    /**
     * 是否有毒
     */
    @TableField(value = "ISTOXIC")
    @Schema(description = "是否有毒")
    private String istoxic;

    /**
     * 存储方式
     */
    @TableField(value = "STORAGEMODE")
    @Schema(description = "存储方式")
    private String storagemode;

    /**
     * 使用场景
     */
    @TableField(value = "USESCENE")
    @Schema(description = "使用场景")
    private String usescene;

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