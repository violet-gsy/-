package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 评估模板表
 * @TableName T_ASS_TEMPLATE
 */
@TableName(value ="T_ASS_TEMPLATE")
@Schema(description = "评估模板表实体")
@Data
public class TAssTemplate implements Serializable {
    /**
     * 模板id
     */
    @TableId(value = "TEMPLATEID",type = IdType.ASSIGN_UUID)
    @Schema(description = "模板id")
    private String templateid;

    /**
     * 模板名称
     */
    @TableField(value = "TEMPLATENAME")
    @Schema(description = "模板名称")
    private String templatename;

    /**
     * 模板说明
     */
    @TableField(value = "REMARK")
    @Schema(description = "模板说明")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "CREATETIME",fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createtime;

    /**
     * 更新时间
     */
    @TableField(value = "UPDATETIME")
    @Schema(description = "更新时间")
    private LocalDateTime updatetime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}