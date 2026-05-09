package com.jc.vo;

import lombok.Data;
import java.util.List;

@Data
public class ProcessDiagramVO {
    // BPMN XML 字符串
    private String bpmnXml;

    // 已完成节点ID
    private List<String> finishedNodeIds;

    // 当前活动节点ID
    private List<String> activeNodeIds;
}
