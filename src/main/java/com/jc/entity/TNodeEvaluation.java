package com.jc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 节点评估项关联表
 * @TableName T_NODE_EVALUATION
 */
@TableName(value ="T_NODE_EVALUATION")
@Schema(description = "节点评估项关联表实体")
@Data
public class TNodeEvaluation implements Serializable {
    /**
     * 关联表ID
     */
    @TableId(value = "ID",type = IdType.ASSIGN_UUID)
    @Schema(description = "关联表ID")
    private String id;

    /**
     * 流程定义ID
     */
    @TableField(value = "PROC_DEF_ID_")
    @Schema(description = "流程定义ID")
    private String procDefId;

    /**
     * 流程节点ID
     */
    @TableField(value = "ACT_ID_")
    @Schema(description = "流程节点ID")
    private String actId;

    /**
     * 评估项IDS
     */
    @TableField(value = "ITEMIDS")
    @Schema(description = "评估项IDS")
    private String itemids;

    /**
     * 评估项名称S
     */
    @TableField(value = "ITEMNAMES")
    @Schema(description = "评估项名称S")
    private String itemnames;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}