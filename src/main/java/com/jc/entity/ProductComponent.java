package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 产品组件表
 * @TableName T_PRODUCT_COMPONENT
 */
@TableName(value ="T_PRODUCT_COMPONENT")
@Schema(description = "产品组件表实体")
@Data
public class ProductComponent implements Serializable {
    /**
     * 组件ID
     */
    @TableId(value = "ID",type = IdType.ASSIGN_UUID)
    @Schema(description = "组件ID")
    private String id;

    /**
     * 产品ID
     */
    @TableField(value = "PRODUCTID")
    @Schema(description = "产品ID")
    private String productid;

    /**
     * 组件名称
     */
    @TableField(value = "NAME")
    @Schema(description = "组件名称")
    private String name;

    /**
     * 组件类型
     */
    @TableField(value = "TYPE")
    @Schema(description = "组件类型")
    private String type;

    /**
     * 父组件 ID
     */
    @TableField(value = "PARENTID")
    @Schema(description = "父组件 ID")
    private String parentid;

    /**
     * 研制 / 生产单位
     */
    @TableField(value = "MANUFACTURER")
    @Schema(description = "生产单位")
    private String manufacturer;

    /**
     * 出厂日期
     */
    @TableField(value = "PRODUCEDATE")
    @Schema(description = "出厂日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime producedate;


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