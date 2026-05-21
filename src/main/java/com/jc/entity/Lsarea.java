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
 * 发射场区域
 * @TableName T_LSAREA
 */
@TableName(value ="T_LSAREA")
@Data
public class Lsarea implements Serializable {
    /**
     * 区域id
     */
    @TableId(value = "AREAID")
    private String areaid;

    /**
     * 发射场id
     */
    @TableField(value = "LSID")
    private String lsid;

    /**
     * 区域名称
     */
    @TableField(value = "AREANAME")
    private String areaname;

    /**
     * 面积
     */
    @TableField(value = "ACREAGE")
    private BigDecimal acreage;

    /**
     * 经度
     */
    @TableField(value = "LONGITUDE")
    private BigDecimal longitude;

    /**
     * 纬度
     */
    @TableField(value = "LATITUDE")
    private BigDecimal latitude;

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