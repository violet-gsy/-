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
 * 发射场关键设施表
 * @TableName T_LS_KEYFACILITY
 */
@TableName(value ="T_LS_KEYFACILITY")
@Data
public class LsKeyfacility implements Serializable {
    /**
     * 设施id
     */
    @TableId(value = "FACILITYID")
    private String facilityid;

    /**
     * 区域id
     */
    @TableField(value = "AREAID")
    private String areaid;

    /**
     * 设施名称
     */
    @TableField(value = "FACILITYNAME")
    private String facilityname;

    /**
     * 设施类型
     */
    @TableField(value = "FACILITYTYPE")
    private String facilitytype;

    /**
     * 是否关键设施
     */
    @TableField(value = "ISKEY")
    private Integer iskey;

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
     * 建筑面积
     */
    @TableField(value = "BUILDAREA")
    private BigDecimal buildarea;

    /**
     * 位置描述
     */
    @TableField(value = "LOCATIONDESC")
    private String locationdesc;

    /**
     * 主要功能
     */
    @TableField(value = "FACILITYDESC")
    private String facilitydesc;

    /**
     * 设施状态
     */
    @TableField(value = "STATE")
    private String state;

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