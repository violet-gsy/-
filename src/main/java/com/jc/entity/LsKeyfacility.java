package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 发射场关键设施表
 * @TableName T_LS_KEYFACILITY
 */
@TableName(value ="T_LS_KEYFACILITY")
@Schema(description = "发射场关键设施表实体")
@Data
public class LsKeyfacility implements Serializable {
    /**
     * 设施id
     */
    @TableId(value = "ID",type = IdType.ASSIGN_UUID)
    @Schema(description = "设施id")
    private String id;


    /**
     * 设施名称
     */
    @TableField(value = "NAME")
    @Schema(description = "设施名称")
    private String name;

    /**
     * 设施类型
     */
    @TableField(value = "TYPE")
    @Schema(description = "设施类型")
    private String type;

    /**
     * 是否关键设施
     */
    @TableField(value = "ISKEY")
    @Schema(description = "是否关键设施")
    private String iskey;

    /**
     * 长
     */
    @TableField(value = "LENGTH")
    @Schema(description = "长")
    private BigDecimal length;

    /**
     * 宽
     */
    @TableField(value = "WIDTH")
    @Schema(description = "宽")
    private BigDecimal width;

    /**
     * 高
     */
    @TableField(value = "HEIGHT")
    @Schema(description = "高")
    private BigDecimal height;

    /**
     * 位置描述
     */
    @TableField(value = "LOCATIONDESC")
    @Schema(description = "位置描述")
    private String locationdesc;

    /**
     * 主要功能
     */
    @TableField(value = "FACILITYDESC")
    @Schema(description = "主要功能")
    private String facilitydesc;

    /**
     * 设施状态
     */
    @TableField(value = "STATUS")
    @Schema(description = "设施状态")
    private String status;

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