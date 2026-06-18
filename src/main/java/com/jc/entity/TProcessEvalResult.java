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
 * 流程评估结果表
 * @TableName T_PROCESS_EVAL_RESULT
 */
@TableName(value ="T_PROCESS_EVAL_RESULT")
@Schema(description = "流程评估结果表实体")
@Data
public class TProcessEvalResult implements Serializable {
    /**
     * 评估结果ID
     */
    @TableId(value = "RESULTID",type = IdType.ASSIGN_UUID)
    @Schema(description = "评估结果ID")
    private String resultid;

    /**
     * 流程记录ID
     */
    @TableField(value = "RECORDID")
    @Schema(description = "流程记录ID")
    private String recordid;

    /**
     * 评估项ID
     */
    @TableField(value = "ITEMID")
    @Schema(description = "评估项ID")
    private String itemid;

    /**
     * 流程综合评估总分
     */
    @TableField(value = "SCORE")
    @Schema(description = "流程综合评估总分")
    private BigDecimal score;

    /**
     * 评估等级
     */
    @TableField(value = "EVALLEVEL")
    @Schema(description = "评估等级")
    private String evallevel;

    /**
     * 整体评估结论
     */
    @TableField(value = "EVALSTATUS")
    @Schema(description = "整体评估结论")
    private String evalstatus;

    /**
     * 问题汇总
     */
    @TableField(value = "SUMMARY")
    @Schema(description = "问题汇总")
    private String summary;

    /**
     * 优化建议
     */
    @TableField(value = "SUGGESTION")
    @Schema(description = "优化建议")
    private String suggestion;

    /**
     * 结果生成时间
     */
    @TableField(value = "CREATETIME")
    @Schema(description = "结果生成时间")
    private LocalDateTime createtime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}