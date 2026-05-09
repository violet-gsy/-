package com.jc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 流程跳转/驳回/退回 入参
 * 作用：将流程强制跳转到任意指定审批节点
 */
@Data
@Schema(name = "ProcessJumpVo", description = "流程跳转输入")
public class ProcessJumpVo {

    @Schema(description = "当前任务ID【必填】")
    private String taskId;

    @Schema(description = "目标节点ID【必填，从下方清单选】")
    private String targetNodeId;

    @Schema(description = "跳转备注/原因", example = "跳转节点")
    private String comment;
}
