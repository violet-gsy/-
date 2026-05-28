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
 * 发射场关键设施表
 * @TableName T_LS_KEYFACILITY
 */
@TableName(value ="T_LS_KEYFACILITY")
@Schema(description = "发射场关键设施表实体")
@Data
public class LsKeyfacility implements Serializable {
    /**
     * 设施id
     */
    @TableId(value = "FACILITYID",type = IdType.ASSIGN_UUID)
    @Schema(description = "设施id")
    private String facilityid;

    /**
     * 区域id
     */
    @TableField(value = "AREAID")
    @Schema(description = "区域id")
    private String areaid;

    /**
     * 设施名称
     */
    @TableField(value = "FACILITYNAME")
    @Schema(description = "设施名称")
    private String facilityname;

    /**
     * 设施类型
     */
    @TableField(value = "FACILITYTYPE")
    @Schema(description = "设施类型")
    private String facilitytype;

    /**
     * 是否关键设施
     */
    @TableField(value = "ISKEY")
    @Schema(description = "是否关键设施")
    private Integer iskey;

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
     * 建筑面积
     */
    @TableField(value = "BUILDAREA")
    @Schema(description = "建筑面积")
    private BigDecimal buildarea;

    /**
     * 位置描述
     */
    @TableField(value = "LOCATIONDESC")
    @Schema(description = "位置描述")
    private String locationdesc;

    /**
     * 主要功能
     */
    @TableField(value = "FACILITYDESC")
    @Schema(description = "主要功能")
    private String facilitydesc;

    /**
     * 设施状态
     */
    @TableField(value = "STATE")
    @Schema(description = "设施状态")
    private String state;

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