package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 接口关系表
 * @TableName T_INTERFACE
 */
@TableName(value ="T_INTERFACE")
@Schema(description = "接口关系实体")
@Data
public class TInterface implements Serializable {
    /**
     * ID
     */
    @TableId(value = "ID",type = IdType.ASSIGN_UUID)
    @Schema(description = "接口关系ID")
    private String id;

    /**
     * 名称
     */
    @TableField(value = "NAME")
    @Schema(description = "名称")
    private String name;

    /**
     * 类型
     */
    @TableField(value = "TYPE")
    @Schema(description = "类型")
    private String type;

    /**
     * 连接对象
     */
    @TableField(value = "CONNECTOR")
    @Schema(description = "连接对象")
    private String connector;

    /**
     * 被连接对象
     */
    @TableField(value = "CONNECTED")
    @Schema(description = "被连接对象")
    private String connected;

    /**
     * 状态
     */
    @TableField(value = "STATUS")
    @Schema(description = "状态")
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

    /**
     * 所属分系统
     */
    @TableField(value = "SUBSYSTEM")
    @Schema(description = "所属分系统")
    private String subsystem;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}