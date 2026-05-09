package com.jc.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;
import java.util.Set;

/**
 * 前端渲染流程图 + 高亮进度 的完整数据
 */
@Data
@Schema(name = "FlowChartVO", description = "流程图进度数据（前端画图专用）")
public class FlowChartVO {

    @Schema(description = "流程实例ID")
    private String processInstanceId;

    @Schema(description = "业务ID")
    private String businessKey;

    @Schema(description = "流程定义 Key")
    private String processKey;

    @Schema(description = "所有节点列表（用于画节点）")
    private List<FlowNode> nodeList;

    @Schema(description = "所有连线列表（用于画箭头）")
    private List<FlowEdge> edgeList;

    @Schema(description = "【高亮】当前正在执行的节点ID集合")
    private Set<String> activeNodeIds;

    @Schema(description = "【高亮】已经执行完成的节点ID集合")
    private Set<String> finishedNodeIds;

    @Data
    public static class FlowNode {
        @Schema(description = "节点ID（唯一）")
        private String nodeId;

        @Schema(description = "节点名称")
        private String nodeName;

        @Schema(description = "节点类型： startEvent    → 开始（圆形）\n" +
                "endEvent      → 结束（圆形）\n" +
                "userTask      → 审批任务（矩形）\n" +
                "gateway       → 判断网关（菱形）\n" +
                "parallel      → 并行网关（菱形）\n" +
                "subProcess    → 子流程（大矩形/折叠框）")
        private String nodeType;

        @Schema(description = "任务ID（跳转用）")
        private String taskId;

        @Schema(description = "父节点ID（子流程ID）")
        private String parentId;

        @Schema(description = "父节点名称（子流程名称）")
        private String parentName;

        @Schema(description = "坐标x")
        private Double x;

        @Schema(description = "坐标y")
        private Double y;
    }

    @Data
    public static class FlowEdge {
        @Schema(description = "连线ID")
        private String edgeId;

        @Schema(description = "来源节点ID")
        private String source;

        @Schema(description = "目标节点ID")
        private String target;

        @Schema(description = "连线备注")
        private String remark;
    }
}
