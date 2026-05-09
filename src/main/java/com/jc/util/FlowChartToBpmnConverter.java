package com.jc.util;

import cn.hutool.core.util.StrUtil;
import com.jc.entity.Nodeattr;
import com.jc.vo.JsonToFlowVO;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class FlowChartToBpmnConverter {

    public BpmnModel convert(JsonToFlowVO vo) {
        BpmnModel model = new BpmnModel();
        Process process = new Process();
        process.setId(vo.getProcessKey());
        process.setName(vo.getProcessKey() + "流程");
        model.addProcess(process);

        List<JsonToFlowVO.FlowNode> nodes = vo.getNodeList();
        List<JsonToFlowVO.FlowEdge> edges = vo.getEdgeList();
        Map<String, JsonToFlowVO.FlowNode> nodeMap = nodes.stream()
                .collect(Collectors.toMap(JsonToFlowVO.FlowNode::getNodeId, n -> n));

        // 1. 先创建所有子流程容器
        Map<String, SubProcess> subProcessMap = new HashMap<>();
        for (JsonToFlowVO.FlowNode node : nodes) {
            if ("subProcess".equals(node.getNodeType())) {
                SubProcess sub = new SubProcess();
                sub.setId(node.getNodeId());
                sub.setName(node.getNodeName());
                process.addFlowElement(sub);
                subProcessMap.put(node.getNodeId(), sub);

                // ========== ✅ Flowable 6.3.0 子流程坐标 ==========
                GraphicInfo subGraphic = new GraphicInfo();
                subGraphic.setX(node.getX());
                subGraphic.setY(node.getY());
                model.addGraphicInfo(sub.getId(), subGraphic);
            }
        }

        // 2. 创建所有节点（用户任务固定审批人 admin）
        for (JsonToFlowVO.FlowNode node : nodes) {
            FlowElement element = buildElement(node);
            if (element == null) continue;

            GraphicInfo graphicInfo = new GraphicInfo();
            graphicInfo.setX(node.getX());
            graphicInfo.setY(node.getY());
            model.addGraphicInfo(element.getId(), graphicInfo);

            if (node.getParentId() != null && subProcessMap.containsKey(node.getParentId())) {
                subProcessMap.get(node.getParentId()).addFlowElement(element);
            } else if (!"subProcess".equals(node.getNodeType())) {
                process.addFlowElement(element);
            }

            //保存节点属性
            addNodeattr(node,vo.getProcessKey());
        }

        // 3. 创建连线（支持条件）
        for (JsonToFlowVO.FlowEdge edge : edges) {
            SequenceFlow seq = new SequenceFlow();
            seq.setId(edge.getEdgeId());
            seq.setSourceRef(edge.getSource());
            seq.setTargetRef(edge.getTarget());

            // 把前端的备注存进去 ✅
            if (StrUtil.isNotBlank(edge.getRemark())) {
                seq.setName(edge.getRemark());
            }

            if (edge.getCondition() != null && !edge.getCondition().isEmpty()) {
                seq.setConditionExpression(edge.getCondition());
            }

            JsonToFlowVO.FlowNode sourceNode = nodeMap.get(edge.getSource());
            if (sourceNode != null && sourceNode.getParentId() != null) {
                subProcessMap.get(sourceNode.getParentId()).addFlowElement(seq);
            } else {
                process.addFlowElement(seq);
            }
        }

        return model;
    }

    private void addNodeattr(JsonToFlowVO.FlowNode node,String key) {
        Nodeattr nodeattr = Nodeattr.builder()
                .nodeattrid(DmUuidUtil.get32Uuid())
                .key(key)
                .pnodeid(node.getNodeId())
                .pnodename(node.getNodeName())
                .nodetype(node.getNodetiertype())
                .eqpids(StringListUtil.listToSemicolonStr(node.getEqpids()))
                .facilityids(StringListUtil.listToSemicolonStr(node.getFacilityids()))
                .toolids(StringListUtil.listToSemicolonStr(node.getToolids()))
                .tooleqpids(StringListUtil.listToSemicolonStr(node.getTooleqpids()))
                .actiontype(node.getActiontype())
                .description(node.getDescription())
                .goal(node.getGoal())
                .modelattr(node.getModelattr())
                .build();
    }

    private FlowElement buildElement(JsonToFlowVO.FlowNode node) {
        String type = node.getNodeType();
        FlowElement flowElement = null;

        switch (type) {
            case "startEvent":
                StartEvent startEvent = new StartEvent();
                startEvent.setId(node.getNodeId());
                startEvent.setName(node.getNodeName());
                flowElement = startEvent;
                break;

            case "endEvent":
                EndEvent endEvent = new EndEvent();
                endEvent.setId(node.getNodeId());
                endEvent.setName(node.getNodeName());
                flowElement = endEvent;
                break;

            case "userTask":
                UserTask userTask = new UserTask();
                userTask.setId(node.getNodeId());
                userTask.setName(node.getNodeName());
                userTask.setAssignee("admin"); // 固定审批人 admin
                flowElement = userTask;
                break;

            case "gateway":
                ExclusiveGateway gateway = new ExclusiveGateway();
                gateway.setId(node.getNodeId());
                gateway.setName(node.getNodeName());
                flowElement = gateway;
                break;

            case "parallel":
                ParallelGateway parallelGateway = new ParallelGateway();
                parallelGateway.setId(node.getNodeId());
                parallelGateway.setName(node.getNodeName());
                flowElement = parallelGateway;
                break;

            case "subProcess":
                flowElement = null;
                break;

            default:
                throw new RuntimeException("不支持的节点类型：" + type);
        }

        return flowElement;
    }

}
