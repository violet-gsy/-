package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @TableId(value = "ID",type = IdType.ASSIGN_UUID)
    @Schema(description = "产品ID")
    private String id;


    /**
     * 产品名称
     */
    @TableField(value = "NAME")
    @Schema(description = "产品名称")
    private String name;

    /**
     * 产品类型
     */
    @TableField(value = "TYPE")
    @Schema(description = "产品类型")
    private String type;

    /**
     * 产品状态
     */
    @TableField(value = "STATUS")
    @Schema(description = "产品状态")
    private String status;

    /**
     * 研制 / 生产单位
     */
    @TableField(value = "MANUFACTURER")
    @Schema(description = "研制单位")
    private String manufacturer;

    /**
     * 出厂日期
     */
    @TableField(value = "MANUFACTURERDATE")
    @Schema(description = "出厂日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime manufacturerdate;

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

    @TableField(value = "SUBSYSTEM")
    @Schema(description = "所属分系统")
    private String subsystem;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}