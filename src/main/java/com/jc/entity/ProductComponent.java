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
    @TableId(value = "COMPONENTID",type = IdType.ASSIGN_UUID)
    @Schema(description = "组件ID")
    private String componentid;

    /**
     * 产品ID
     */
    @TableField(value = "PRODUCTID")
    @Schema(description = "产品ID")
    private String productid;

    /**
     * 组件名称
     */
    @TableField(value = "COMPONENTNAME")
    @Schema(description = "组件名称")
    private String componentname;

    /**
     * 组件类型
     */
    @TableField(value = "COMPONENTTYPE")
    @Schema(description = "组件类型")
    private String componenttype;

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
    @Schema(description = "研制 / 生产单位")
    private String manufacturer;

    /**
     * 出厂日期
     */
    @TableField(value = "PRODUCEDATE")
    @Schema(description = "出厂日期")
    private LocalDateTime producedate;

    /**
     * 技术状态
     */
    @TableField(value = "STATUS")
    @Schema(description = "技术状态")
    private String status;

    /**
     * 备注
     */
    @TableField(value = "REMARK")
    @Schema(description = "备注")
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}