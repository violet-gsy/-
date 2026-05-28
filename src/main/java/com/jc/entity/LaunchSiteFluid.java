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
     * 计量单位ID
     */
    @TableField(value = "UNITID")
    @Schema(description = "计量单位ID")
    private String unitid;

    /**
     * 介质编码
     */
    @TableField(value = "FLUIDCODE")
    @Schema(description = "介质编码")
    private String fluidcode;

    /**
     * 介质名称
     */
    @TableField(value = "FLUIDNAME")
    @Schema(description = "介质名称")
    private String fluidname;

    /**
     * 介质类型
     */
    @TableField(value = "FLUIDTYPE")
    @Schema(description = "介质类型")
    private String fluidtype;

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
    private Integer issupport;

    /**
     * 是否有毒
     */
    @TableField(value = "ISTOXIC")
    @Schema(description = "是否有毒")
    private Integer istoxic;

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
    @TableField(value = "CREATETIME")
    @Schema(description = "创建时间")
    private LocalDateTime createtime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}