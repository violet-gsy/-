package com.jc.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Set;


@Data
@Schema(name = "JsonToFlowVO", description = "json转flow，部署流程")
public class JsonToFlowVO {


    @Schema(description = "流程定义KEY,新增时传空，修改时传后端给返回的")
    private String processKey;

    @Schema(description = "所有节点列表（用于画节点）")
    private List<FlowNode> nodeList;

    @Schema(description = "所有连线列表（用于画箭头）")
    private List<FlowEdge> edgeList;

    @Data
    public static class FlowNode {
        @Schema(description = "节点ID（唯一）")
        private String nodeId;

        @Schema(description = "节点名称")
        private String nodeName;

        @Schema(description = "节点类型： " +
                "startEvent    → 开始（圆形）\n" +
                "endEvent      → 结束（圆形）\n" +
                "userTask      → 审批任务（矩形）\n" +
                "gateway       → 判断网关（菱形）\n" +
                "parallel      → 并行网关（菱形）\n" +
                "subProcess    → 子流程（大矩形/折叠框）")
        private String nodeType;

        @Schema(description = "父节点ID（子流程内部节点使用）")
        private String parentId;

        @Schema(description = "父节点名称")
        private String parentName;

        @Schema(description = "坐标x")
        private Integer x;
        @Schema(description = "坐标y")
        private Integer y;

        @Schema(description = "是否有节点属性,如果有保存以下属性，如果没有则停止解析以下属性字段")
        private boolean haveAttr;

        @Schema(description = "节点类型(0中层/1底层)")
        private Integer nodetiertype;

        @Schema(description = "节点设备IDS")
        private List<String> eqpids;

        @Schema(description = "节点设施IDS")
        private List<String> facilityids;

        @Schema(description = "节点工装IDS")
        private List<String> toolids;

        @Schema(description = "节点工器具IDS")
        private List<String> tooleqpids;

        @Schema(description = "节点动作类型")
        private String actiontype;

        @Schema(description = "节点描述")
        private String description;

        @Schema(description = "节点目标")
        private String goal;

        @Schema(description = "节点属性（三维小步骤）")
        private String modelattr;
    }

    @Data
    public static class FlowEdge {
        @Schema(description = "连线ID")
        private String edgeId;
        @Schema(description = "来源节点ID")
        private String source;
        @Schema(description = "目标节点ID")
        private String target;
        @Schema(description = "网关条件")
        private String condition;
        @Schema(description = "连线备注")
        private String remark;
    }
}
