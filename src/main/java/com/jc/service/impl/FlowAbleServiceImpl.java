package com.jc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jc.entity.Nodeattr;
import com.jc.service.FlowAbleService;
import com.jc.service.NodeattrService;
import com.jc.service.NodeattrhisService;
import com.jc.util.*;
import com.jc.vo.FlowChartVO;
import com.jc.vo.JsonToFlowVO;
import com.jc.vo.NodeAttrOutputVo;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableAsync
@Transactional(rollbackFor = Exception.class)
public class FlowAbleServiceImpl implements FlowAbleService {

    @Resource
    private RepositoryService repositoryService;

    @Resource
    NodeattrService nodeattrService;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

    @Resource
    private HistoryService historyService;

    @Async
    @Transactional
    public void startAutoApproval(String processInstanceId) {
        try {
            while (true) {
                // 查询【这个流程自己】的待办任务（只查自己，不影响别人）
                Task task = taskService.createTaskQuery()
                        .processInstanceId(processInstanceId) // 关键：只查当前流程
                        .singleResult();

                // 没有任务 = 该流程结束
                if (task == null) {
                    log.info("✅ 流程【" + processInstanceId + "】已全部自动审批完成");
                    break;
                }

                // 每 3 秒审批一步
                Thread.sleep(5000);

                // 正常审批逻辑...
                Map<String, Object> vars = new HashMap<>();
                vars.put("auditResult", "agree");
                taskService.complete(task.getId(),vars);

                WebSocketServer.sendMsg(MessageTypeEnum.UPDATENODE,task.getTaskDefinitionKey(),0);

                log.info("⏱ 流程【" + processInstanceId + "】:节点"+task.getTaskDefinitionKey() +"自动审批：" + task.getName());
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 校验流程名称是否重复
     */
    public boolean checkFlowNameExists(String flowName, String processKey) {
        long count = repositoryService.createDeploymentQuery()
                .deploymentName(flowName)
                .count();

        // 修改时：排除自己（通过 processKey 找自己的部署）
        if (StrUtil.isNotBlank(processKey)) {
            // 找到自己最新的部署
            ProcessDefinition selfDef = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(processKey)
                    .latestVersion()
                    .singleResult();

            if (selfDef != null) {
                Deployment selfDeploy = repositoryService.createDeploymentQuery()
                        .deploymentId(selfDef.getDeploymentId())
                        .singleResult();
                // 如果查到的重复名称就是自己，不算重复
                if (selfDeploy != null && flowName.equals(selfDeploy.getName())) {
                    return false;
                }
            }
        }
        return count > 0;
    }

    // 新增空流程：自动生成 processKey
    public ApiResponse createEmptyFlow(String flowName) {
        // 自动生成唯一的 processKey（永远不变）
        String processKey = "key_" + DmUuidUtil.get32Uuid();
        BpmnModel bpmnModel = createMinimalValidBpmn(processKey, flowName);

        // 部署
        Deployment deploy = repositoryService.createDeployment()
                .addBpmnModel(processKey + ".bpmn20.xml", bpmnModel)
                .name(flowName)
                .key(processKey) // 这里把 processKey 绑定到部署上
                .deploy();

        log.info("空流程创建成功，processKey:{}", processKey);
        // 返回 processKey 给前端！这就是你后续编辑用的ID
        return ApiResponse.success("创建成功", processKey);
    }

    public ApiResponse updateFlowName(String processKey, String newName) {
        // 1. 找到旧的流程定义
        ProcessDefinition oldDef = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .latestVersion()
                .singleResult();

        if (oldDef == null) {
            return ApiResponse.error("流程不存在");
        }

        // 2. 完整拿到旧的 BpmnModel！（包含你所有已经编辑的节点！）
        BpmnModel oldBpmn = repositoryService.getBpmnModel(oldDef.getId());

        // 3. 只修改流程名称！其他所有节点、属性完全不动！
        org.flowable.bpmn.model.Process oldProcess = oldBpmn.getMainProcess();
        oldProcess.setName(newName);

        // 4. 删除旧部署
        repositoryService.deleteDeployment(oldDef.getDeploymentId(), true);

        // 5. 用修改了名称的旧模型重新部署（processKey 完全不变！）
        Deployment newDeploy = repositoryService.createDeployment()
                .addBpmnModel(processKey + ".bpmn20.xml", oldBpmn)
                .name(newName)
                .key(processKey)
                .deploy();

        log.info("流程改名成功，processKey:{}，新名称:{}，所有节点已保留", processKey, newName);
        return ApiResponse.success("修改成功", processKey);
    }

    /**
     * 生成空BPMN模型
     */
    private BpmnModel createMinimalValidBpmn(String processKey, String flowName) {
        BpmnModel bpmnModel = new BpmnModel();
        org.flowable.bpmn.model.Process process = new org.flowable.bpmn.model.Process();

        process.setId(processKey);
        process.setName(flowName);
        process.setExecutable(true);

        // 加占位开始节点，满足Flowable校验
        StartEvent startEvent = new StartEvent();
        startEvent.setId("start_placeholder");
        startEvent.setName("开始");
        process.addFlowElement(startEvent);

        bpmnModel.addProcess(process);
        return bpmnModel;
    }

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

        //先清空上次流程中的所有的节点属性，再保存本次的
        nodeattrService.remove(new QueryWrapper<Nodeattr>().eq("KEY_",vo.getProcessKey()));

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
            if (node.isHaveAttr()){
                addNodeattr(node,vo.getProcessKey());
            }
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

    @Override
    public ApiResponse<FlowChartVO> getFlowChart(String processInstanceId) {
        FlowChartVO vo = new FlowChartVO();
        vo.setProcessInstanceId(processInstanceId);

        BpmnModel bpmnModel = null;
        ProcessInstance processInstance = null;

        // ====================== 核心改动：自动识别参数 ======================
        // 1. 先尝试按 实例ID 查询（原来的逻辑）
        processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (processInstance != null) {
            // ✅ 查到实例了：走运行中流程的逻辑
            vo.setBusinessKey(processInstance.getBusinessKey());
            bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        } else {
            // ❌ 没查到实例：说明传的是 processKey，尝试加载流程模板（未启动也能查）
            ProcessDefinition def = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(processInstanceId)
                    .latestVersion()
                    .singleResult();

            if (def != null) {
                // ✅ 查到模板了：加载BPMN模型，用于前端编辑画图
                bpmnModel = repositoryService.getBpmnModel(def.getId());
                vo.setBusinessKey(processInstanceId);
                vo.setProcessKey(def.getKey());
            } else {
                // 连模板都没查到，才返回空
                return ApiResponse.success("获取成功", vo);
            }
        }

        // ====================== 下面通用画图逻辑，无论模板/实例都执行 ======================
        Collection<FlowElement> flowElements = bpmnModel.getMainProcess().getFlowElements();
        List<FlowChartVO.FlowNode> nodeList = new ArrayList<>();
        List<FlowChartVO.FlowEdge> edgeList = new ArrayList<>();

        Map<String, String> allTaskIdMap = new HashMap<>();
        Set<String> finishedNodeIds = new HashSet<>();
        Set<String> activeNodeIds = new HashSet<>();

        // ====================== 只有运行中流程，才加载高亮数据 ======================
        if (processInstance != null) {
            // 当前待办任务
            List<org.flowable.task.api.Task> runningTasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
            for (Task task : runningTasks) {
                allTaskIdMap.put(task.getTaskDefinitionKey(), task.getId());
            }

            // 历史任务
            List<HistoricTaskInstance> historicTasks = historyService
                    .createHistoricTaskInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .list();
            for (HistoricTaskInstance task : historicTasks) {
                allTaskIdMap.putIfAbsent(task.getTaskDefinitionKey(), task.getId());
            }

            // 已完成节点
            List<HistoricActivityInstance> finishedList = historyService
                    .createHistoricActivityInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .finished()
                    .list();
            for (HistoricActivityInstance act : finishedList) {
                finishedNodeIds.add(act.getActivityId());
            }

            // 当前活动节点
            List<Execution> executions = runtimeService.createExecutionQuery()
                    .processInstanceId(processInstanceId)
                    .list();
            for (Execution execution : executions) {
                if (execution.getActivityId() != null && !execution.getActivityId().trim().isEmpty()) {
                    activeNodeIds.add(execution.getActivityId());
                }
            }
        }

        // 解析所有节点和连线（无论是否启动都执行）
        for (FlowElement element : flowElements) {
            parseElement(element,null,null,bpmnModel,allTaskIdMap, nodeList, edgeList);
        }

        vo.setNodeList(nodeList);
        vo.setEdgeList(edgeList);
        vo.setActiveNodeIds(activeNodeIds);
        vo.setFinishedNodeIds(finishedNodeIds);
        if (vo.getProcessKey() == null) {
            vo.setProcessKey(bpmnModel.getMainProcess().getId());
        }
        return ApiResponse.success(vo);
    }



    /**
     * 递归解析节点（自动带 parent 信息）
     */
    private void parseElement(FlowElement element,
                              String parentId,
                              String parentName,
                              BpmnModel bpmnModel,
                              Map<String, String> taskIdMap,
                              List<FlowChartVO.FlowNode> nodeList,
                              List<FlowChartVO.FlowEdge> edgeList) {

        if (element instanceof SubProcess) {
            SubProcess sub = (SubProcess) element;
            addNode(bpmnModel,element, parentId, parentName, taskIdMap, nodeList);

            for (FlowElement inner : sub.getFlowElements()) {
                parseElement(inner, element.getId(), element.getName(),bpmnModel, taskIdMap, nodeList, edgeList);
            }
            return;
        }

        if (element instanceof FlowNode) {
            addNode(bpmnModel,element, parentId, parentName, taskIdMap, nodeList);
            return;
        }

        if (element instanceof SequenceFlow) {
            SequenceFlow flow = (SequenceFlow) element;
            FlowChartVO.FlowEdge edge = new FlowChartVO.FlowEdge();
            edge.setEdgeId(flow.getId());
            edge.setSource(flow.getSourceRef());
            edge.setTarget(flow.getTargetRef());
            edge.setRemark(flow.getName());
            edgeList.add(edge);
        }
    }



    private void addNode(BpmnModel bpmnModel,FlowElement element,
                         String parentId,
                         String parentName,
                         Map<String, String> taskIdMap,
                         List<FlowChartVO.FlowNode> nodeList) {

        FlowChartVO.FlowNode node = new FlowChartVO.FlowNode();
        node.setNodeId(element.getId());
        node.setNodeName(element.getName());
        node.setNodeType(getNodeType(element));

        // ✅ 正确赋值：运行时真实任务ID
        node.setTaskId(taskIdMap.get(element.getId()));

        node.setParentId(parentId);
        node.setParentName(parentName);
        GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(element.getId());
        if (graphicInfo != null) {
            node.setX(graphicInfo.getX());
            node.setY(graphicInfo.getY());
        }
        nodeList.add(node);
    }

    /**
     * 自动识别节点类型，前端直接分类
     */
    private String getNodeType(FlowElement element) {
        if (element instanceof StartEvent) {
            return "startEvent";
        }
        if (element instanceof EndEvent) {
            return "endEvent";
        }
        if (element instanceof UserTask) {
            return "userTask";
        }
        if (element instanceof SubProcess) {
            return "subProcess";
        }
        if (element instanceof ParallelGateway) {
            return "parallel";
        }
        if (element instanceof ExclusiveGateway) {
            return "gateway";
        }
        return "other";
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
        nodeattrService.save(nodeattr);
    }

    @Override
    public ApiResponse<NodeAttrOutputVo> getNodeAttr(String pnodeid) {
        List<Nodeattr> nodeattrs = nodeattrService.list(new QueryWrapper<Nodeattr>().eq("PNODEID",pnodeid));
        if (nodeattrs.size() == 0){
            return ApiResponse.success("该节点无属性");
        }
        Nodeattr nodeattr = nodeattrs.get(0);
        NodeAttrOutputVo nodeAttrOutputVo = NodeAttrOutputVo.builder()
                .nodeattrid(nodeattr.getNodeattrid())
                .key(nodeattr.getKey())
                .pnodeid(nodeattr.getPnodeid())
                .pnodeName(nodeattr.getPnodename())
                .nodetiertype(nodeattr.getNodetype())
                .eqpids(StringListUtil.semicolonStrToList(nodeattr.getEqpids()))
                .facilityids(StringListUtil.semicolonStrToList(nodeattr.getFacilityids()))
                .toolids(StringListUtil.semicolonStrToList(nodeattr.getToolids()))
                .tooleqpids(StringListUtil.semicolonStrToList(nodeattr.getTooleqpids()))
                .actiontype(nodeattr.getActiontype())
                .description(nodeattr.getDescription())
                .goal(nodeattr.getGoal())
                .modelattr(nodeattr.getModelattr())
                .build();
        return ApiResponse.success(nodeAttrOutputVo);
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
