package com.jc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jc.entity.SimulationNodeRecord;

import java.util.List;
import java.util.Map;

public interface SimulationNodeRecordService extends IService<SimulationNodeRecord> {

    List<SimulationNodeRecord> getRecordsByRunId(String runId);

    SimulationNodeRecord getRecordByRunIdAndNodeId(String runId, String nodeId);

    SimulationNodeRecord getRecordByRunIdAndNodeName(String runId, String nodeName);

    List<SimulationNodeRecord> getRunningNodes(String runId);

    List<SimulationNodeRecord> getWaitingNodes(String runId);

    List<SimulationNodeRecord> getCompletedNodes(String runId);

    Integer getCompletedCount(String runId);

    Long getAvgExecutionTime(String runId);

    /**
     * 记录节点就绪（包含所有变量）
     */
    void recordNodeReady(String runId, String nodeId, String nodeName, String nodeType,
                         Integer requiredPeople, Boolean isStartNode, Boolean isEndNode,
                         String description, Map<String, Object> allProperties,
                         String stereotypeName);

    void recordNodeStart(String runId, String nodeId);

    /**
     * 记录节点完成（不保存最终变量）
     */
    void recordNodeFinish(String runId, String nodeId);

    /**
     * 记录节点完成（保存最终变量）
     */
    void recordNodeFinish(String runId, String nodeId, Map<String, Object> finalVars);

    void recordNodeWaiting(String runId, String nodeId, Integer queuePosition, Integer queueSize);

    void recordNodeError(String runId, String nodeId, String errorMessage);

    void updateNodeStatus(String runId, String nodeId, String status);

    /**
     * 更新节点变量
     */
    void updateNodeVars(String runId, String nodeId, Map<String, Object> vars);

    /**
     * 获取节点变量
     */
    Map<String, Object> getNodeVars(String runId, String nodeId);

    Map<String, Object> getNodeStatistics(String runId, String nodeId);

    Map<String, Object> getRunStatistics(String runId);
}