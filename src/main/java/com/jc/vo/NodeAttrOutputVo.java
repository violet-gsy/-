package com.jc.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;


@Data
@Schema(name = "NodeAttrOutputVo", description = "节点属性详细数据")
@Builder
public class NodeAttrOutputVo {

    @Schema(description = "节点属性ID")
    private String nodeattrid;

    @Schema(description = "流程定义ID")
    private String key;

    @Schema(description = "流程节点ID")
    private String pnodeid;

    @Schema(description = "节点名称")
    private String pnodeName;

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
