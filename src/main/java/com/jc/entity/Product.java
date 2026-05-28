package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 产品表
 * @TableName T_PRODUCT
 */
@TableName(value ="T_PRODUCT")
@Schema(description = "产品表实体")
@Data
public class Product implements Serializable {
    /**
     * 产品ID
     */
    @TableId(value = "PRODUCTID",type = IdType.ASSIGN_UUID)
    @Schema(description = "产品ID")
    private String productid;

    /**
     * 发射场id
     */
    @TableField(value = "LSID")
    @Schema(description = "发射场id")
    private String lsid;

    /**
     * 产品名称
     */
    @TableField(value = "PRODUCTNAME")
    @Schema(description = "产品名称")
    private String productname;

    /**
     * 产品类型
     */
    @TableField(value = "PRODUCTTYPE")
    @Schema(description = "产品类型")
    private String producttype;

    /**
     * 产品状态
     */
    @TableField(value = "PRODUCTSTATUS")
    @Schema(description = "产品状态")
    private String productstatus;

    /**
     * 研制 / 生产单位
     */
    @TableField(value = "MANUFACTURER")
    @Schema(description = "研制 / 生产单位")
    private String manufacturer;

    /**
     * 出厂日期
     */
    @TableField(value = "PRODUCEDATE")
    @Schema(description = "出厂日期")
    private LocalDateTime producedate;

    /**
     * 备注
     */
    @TableField(value = "REMARK")
    @Schema(description = "备注")
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}