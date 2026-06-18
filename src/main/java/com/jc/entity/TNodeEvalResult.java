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
 * 节点评估结果表
 * @TableName T_NODE_EVAL_RESULT
 */
@TableName(value ="T_NODE_EVAL_RESULT")
@Schema(description = "节点评估结果表实体")
@Data
public class TNodeEvalResult implements Serializable {
    /**
     * 评估结果ID
     */
    @TableId(value = "RESULTID",type = IdType.ASSIGN_UUID)
    @Schema(description = "评估结果ID")
    private String resultid;

    /**
     * 节点记录ID
     */
    @TableField(value = "RECORDID")
    @Schema(description = "节点记录ID")
    private String recordid;

    /**
     * 关联表ID
     */
    @TableField(value = "GLID")
    @Schema(description = "关联表ID")
    private String glid;

    /**
     * 可达性
     */
    @TableField(value = "ACCESSIBILITY")
    @Schema(description = "可达性")
    private String accessibility;

    /**
     * 仿真实际值
     */
    @TableField(value = "FZVALUE")
    @Schema(description = "仿真实际值")
    private BigDecimal fzvalue;

    /**
     * 该项指标得分
     */
    @TableField(value = "SCORE")
    @Schema(description = "该项指标得分")
    private BigDecimal score;

    /**
     * 评估结果
     */
    @TableField(value = "EVALSTATUS")
    @Schema(description = "评估结果")
    private String evalstatus;

    /**
     * 评估说明
     */
    @TableField(value = "REMARK")
    @Schema(description = "评估说明")
    private String remark;

    /**
     * 结果生成时间
     */
    @TableField(value = "CREATETIME")
    @Schema(description = "结果生成时间")
    private LocalDateTime createtime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}