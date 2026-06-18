package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @TableId(value = "ID",type = IdType.ASSIGN_UUID)
    @Schema(description = "区域id")
    private String id;


    /**
     * 区域名称
     */
    @TableField(value = "NAME")
    @Schema(description = "区域名称")
    private String name;

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
    @TableField(value = "CREATETIME",fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createtime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}