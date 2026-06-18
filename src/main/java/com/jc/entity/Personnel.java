package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 人员表
 * @TableName T_PERSONNEL
 */
@TableName(value ="T_PERSONNEL")
@Schema(description = "人员表实体")
@Data
public class Personnel implements Serializable {
    /**
     * 工号ID
     */
    @TableId(value = "ID",type = IdType.ASSIGN_UUID)
    @Schema(description = "工号ID")
    private String id;

    /**
     * 名称
     */
    @TableField(value = "NAME")
    @Schema(description = "名称")
    private String name;

    /**
     * 性别
     */
    @TableField(value = "SEX")
    @Schema(description = "性别")
    private String sex;

    /**
     * 工作状态
     */
    @TableField(value = "STATUS")
    @Schema(description = "工作状态（工作中，未工作）")
    private String status;

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