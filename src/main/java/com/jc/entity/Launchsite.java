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
 * 发射场表
 * @TableName T_LAUNCHSITE
 */
@TableName(value ="T_LAUNCHSITE")
@Schema(description = "发射场实体")
@Data
public class Launchsite implements Serializable {
    /**
     * 发射场id
     */
    @TableId(value = "LSID",type = IdType.ASSIGN_UUID)
    @Schema(description = "发射场id")
    private String lsid;

    /**
     * 发射场名称
     */
    @TableField(value = "LSNAME")
    @Schema(description = "发射场名称")
    private String lsname;

    /**
     * 面积
     */
    @TableField(value = "ACREAGE")
    @Schema(description = "面积")
    private BigDecimal acreage;

    /**
     * 经度
     */
    @TableField(value = "LONGITUDE")
    @Schema(description = "经度")
    private BigDecimal longitude;

    /**
     * 纬度
     */
    @TableField(value = "LATITUDE")
    @Schema(description = "纬度")
    private BigDecimal latitude;

    /**
     * 地质条件
     */
    @TableField(value = "GEOLCOND")
    @Schema(description = "地质条件")
    private String geolcond;

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