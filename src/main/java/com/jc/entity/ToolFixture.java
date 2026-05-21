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
 * 工装表
 * @TableName T_TOOL_FIXTURE
 */
@TableName(value ="T_TOOL_FIXTURE")
@Data
public class ToolFixture implements Serializable {
    /**
     * 工装id
     */
    @TableId(value = "TOOLID")
    private String toolid;

    /**
     * 设施id
     */
    @TableField(value = "FACILITYID")
    private String facilityid;

    /**
     * 专业ID
     */
    @TableField(value = "MAJORID")
    private String majorid;

    /**
     * 产品ID
     */
    @TableField(value = "PRODUCTID")
    private String productid;

    /**
     * 工装名称
     */
    @TableField(value = "TOOLNAME")
    private String toolname;

    /**
     * 工装类型
     */
    @TableField(value = "TOOLTYPE")
    private String tooltype;

    /**
     * 状态
     */
    @TableField(value = "STATUS")
    private String status;

    /**
     * 生产厂家
     */
    @TableField(value = "MANUFACTURER")
    private String manufacturer;

    /**
     * 出厂日期
     */
    @TableField(value = "PRODUCEDATE")
    private LocalDateTime producedate;

    /**
     * 启用日期
     */
    @TableField(value = "INSTALLDATE")
    private LocalDateTime installdate;

    /**
     * 使用年限
     */
    @TableField(value = "SERVICELIFE")
    private BigDecimal servicelife;

    /**
     * 存放位置
     */
    @TableField(value = "LOCATION")
    private String location;

    /**
     * 备注
     */
    @TableField(value = "REMARK")
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}