package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 评估模型管理表
 * @TableName T_EVALUATION_MODEL
 */
@TableName(value ="T_EVALUATION_MODEL")
@Schema(description = "评估模型管理表实体")
@Data
public class TEvaluationModel implements Serializable {
    /**
     * 评估模型ID
     */
    @TableId(value = "EVALMODELID",type = IdType.ASSIGN_UUID)
    @Schema(description = "评估模型ID")
    private String evalmodelid;

    /**
     * 模型名称
     */
    @TableField(value = "EVALMODENAME")
    @Schema(description = "模型名称")
    private String evalmodename;

    /**
     * 计算规则
     */
    @TableField(value = "CALCRULE")
    @Schema(description = "计算规则")
    private String calcrule;

    /**
     * 模型版本号
     */
    @TableField(value = "VERSION")
    @Schema(description = "模型版本号")
    private Integer version;

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
     * 更新时间
     */
    @TableField(value = "UPDATETIME")
    @Schema(description = "更新时间")
    private LocalDateTime updatetime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}