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
 * 发射场区域
 * @TableName T_LSAREA
 */
@TableName(value ="T_LSAREA")
@Schema(description = "发射场区域实体")
@Data
public class Lsarea implements Serializable {
    /**
     * 区域id
     */
    @TableId(value = "AREAID",type = IdType.ASSIGN_UUID)
    @Schema(description = "区域id")
    private String areaid;

    /**
     * 发射场id
     */
    @TableField(value = "LSID")
    @Schema(description = "发射场id")
    private String lsid;

    /**
     * 区域名称
     */
    @TableField(value = "AREANAME")
    @Schema(description = "区域名称")
    private String areaname;

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