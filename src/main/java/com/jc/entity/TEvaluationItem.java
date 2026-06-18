package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 评估项明细表
 * @TableName T_EVALUATION_ITEM
 */
@TableName(value ="T_EVALUATION_ITEM")
@Schema(description = "评估项明细表实体")
@Data
public class TEvaluationItem implements Serializable {
    /**
     * 评估项ID
     */
    @TableId(value = "ITEMID",type = IdType.ASSIGN_UUID)
    @Schema(description = "评估项ID")
    private String itemid;

    /**
     * 模板ID
     */
    @TableField(value = "TEMPLATEID")
    @Schema(description = "模板ID")
    private String templateid;

    /**
     * 评估项名称
     */
    @TableField(value = "ITEMNAME")
    @Schema(description = "评估项名称")
    private String itemname;

    /**
     * 满分分值
     */
    @TableField(value = "FULLSCORE")
    @Schema(description = "满分分值")
    private BigDecimal fullscore;

    /**
     * 父id
     */
    @TableField(value = "PARENTID")
    @Schema(description = "父id")
    private String parentid;

    /**
     * 排序号
     */
    @TableField(value = "NO")
    @Schema(description = "排序号")
    private Integer no;

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

    /**
     * 类型
     */
    @TableField(value = "TYPE")
    @Schema(description = "类型")
    private String type;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}