package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 产品组件表
 * @TableName T_PRODUCT_COMPONENT
 */
@TableName(value ="T_PRODUCT_COMPONENT")
@Data
public class ProductComponent implements Serializable {
    /**
     * 组件ID
     */
    @TableId(value = "COMPONENTID")
    private String componentid;

    /**
     * 产品ID
     */
    @TableField(value = "PRODUCTID")
    private String productid;

    /**
     * 组件名称
     */
    @TableField(value = "COMPONENTNAME")
    private String componentname;

    /**
     * 组件类型
     */
    @TableField(value = "COMPONENTTYPE")
    private String componenttype;

    /**
     * 父组件 ID
     */
    @TableField(value = "PARENTID")
    private String parentid;

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
     * 技术状态
     */
    @TableField(value = "STATUS")
    private String status;

    /**
     * 备注
     */
    @TableField(value = "REMARK")
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}