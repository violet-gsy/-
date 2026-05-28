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
 * 工装表
 * @TableName T_TOOL_FIXTURE
 */
@TableName(value ="T_TOOL_FIXTURE")
@Schema(description = "工装表实体")
@Data
public class ToolFixture implements Serializable {
    /**
     * 工装id
     */
    @TableId(value = "TOOLID",type = IdType.ASSIGN_UUID)
    @Schema(description = "工装id")
    private String toolid;

    /**
     * 设施id
     */
    @TableField(value = "FACILITYID")
    @Schema(description = "设施id")
    private String facilityid;

    /**
     * 专业ID
     */
    @TableField(value = "MAJORID")
    @Schema(description = "专业ID")
    private String majorid;

    /**
     * 产品ID
     */
    @TableField(value = "PRODUCTID")
    @Schema(description = "产品ID")
    private String productid;

    /**
     * 工装名称
     */
    @TableField(value = "TOOLNAME")
    @Schema(description = "工装名称")
    private String toolname;

    /**
     * 工装类型
     */
    @TableField(value = "TOOLTYPE")
    @Schema(description = "工装类型")
    private String tooltype;

    /**
     * 状态
     */
    @TableField(value = "STATUS")
    @Schema(description = "状态")
    private String status;

    /**
     * 生产厂家
     */
    @TableField(value = "MANUFACTURER")
    @Schema(description = "生产厂家")
    private String manufacturer;

    /**
     * 出厂日期
     */
    @TableField(value = "PRODUCEDATE")
    @Schema(description = "出厂日期")
    private LocalDateTime producedate;

    /**
     * 启用日期
     */
    @TableField(value = "INSTALLDATE")
    @Schema(description = "启用日期")
    private LocalDateTime installdate;

    /**
     * 使用年限
     */
    @TableField(value = "SERVICELIFE")
    @Schema(description = "使用年限")
    private BigDecimal servicelife;

    /**
     * 存放位置
     */
    @TableField(value = "LOCATION")
    @Schema(description = "存放位置")
    private String location;

    /**
     * 备注
     */
    @TableField(value = "REMARK")
    @Schema(description = "备注")
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}