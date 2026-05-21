package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 产品表
 * @TableName T_PRODUCT
 */
@TableName(value ="T_PRODUCT")
@Data
public class Product implements Serializable {
    /**
     * 产品ID
     */
    @TableId(value = "PRODUCTID")
    private String productid;

    /**
     * 发射场id
     */
    @TableField(value = "LSID")
    private String lsid;

    /**
     * 产品名称
     */
    @TableField(value = "PRODUCTNAME")
    private String productname;

    /**
     * 产品类型
     */
    @TableField(value = "PRODUCTTYPE")
    private String producttype;

    /**
     * 产品状态
     */
    @TableField(value = "PRODUCTSTATUS")
    private String productstatus;

    /**
     * 研制 / 生产单位
     */
    @TableField(value = "MANUFACTURER")
    private String manufacturer;

    /**
     * 出厂日期
     */
    @TableField(value = "PRODUCEDATE")
    private LocalDateTime producedate;

    /**
     * 备注
     */
    @TableField(value = "REMARK")
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}