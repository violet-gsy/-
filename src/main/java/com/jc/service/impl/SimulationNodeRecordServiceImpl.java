package com.jc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.entity.SimulationNodeRecord;
import com.jc.mapper.SimulationNodeRecordMapper;
import com.jc.service.SimulationNodeRecordService;
import com.jc.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SimulationNodeRecordServiceImpl extends ServiceImpl<SimulationNodeRecordMapper, SimulationNodeRecord>
        implements SimulationNodeRecordService {

    private int executionOrderCounter = 0;

    @Override
    public List<SimulationNodeRecord> getRecordsByRunId(String runId) {
        return baseMapper.selectByRunId(runId);
    }

    @Override
    public SimulationNodeRecord getRecordByRunIdAndNodeId(String runId, String nodeId) {
        return baseMapper.selectByRunIdAndNodeId(runId, nodeId);
    }

    @Override
    public SimulationNodeRecord getRecordByRunIdAndNodeName(String runId, String nodeName) {
        return baseMapper.selectByRunIdAndNodeName(runId, nodeName);
    }

    @Override
    public List<SimulationNodeRecord> getRunningNodes(String runId) {
        return baseMapper.selectRunningNodes(runId);
    }

    @Override
    public List<SimulationNodeRecord> getWaitingNodes(String runId) {
        return baseMapper.selectWaitingNodes(runId);
    }

    @Override
    public List<SimulationNodeRecord> getCompletedNodes(String runId) {
        return baseMapper.selectCompletedNodes(runId);
    }

    @Override
    public Integer getCompletedCount(String runId) {
        return baseMapper.countCompletedNodes(runId);
    }

    @Override
    public Long getAvgExecutionTime(String runId) {
        return baseMapper.selectAvgExecutionTime(runId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordNodeReady(String runId, String nodeId, String nodeName, String nodeType,
                                Integer requiredPeople, Boolean isStartNode, Boolean isEndNode,
                                String description, Map<String, Object> allProperties,
                                String stereotypeName) {

        log.info("[recordNodeReady] runId={}, nodeId={}, nodeName={}, requiredPeople={}, allProperties={}",
                runId, nodeId, nodeName, requiredPeople, allProperties);

        SimulationNodeRecord record = getRecordByRunIdAndNodeId(runId, nodeId);
        boolean isNew = (record == null);

        if (isNew) {
            record = new SimulationNodeRecord();
            record.setRunId(runId);
            record.setNodeId(nodeId);
            record.setNodeName(nodeName);
            record.setNodeType(nodeType);
            record.setExecutionOrder(++executionOrderCounter);
        }

        // 设置基本字段
        int peopleToSet = requiredPeople != null ? requiredPeople : 1;
        record.setRequiredPeople(peopleToSet);
        record.setIsStartNode(isStartNode != null && isStartNode);
        record.setIsEndNode(isEndNode != null && isEndNode);
        record.setNodeDescription(description);
        record.setExecutionStatus("ready");
        record.setReadyTime(LocalDateTime.now());

        // 保存构造型信息
        record.setStereotypeName(stereotypeName);

        // 保存所有属性
        if (allProperties != null && !allProperties.isEmpty()) {
            String propertiesJson = JsonUtils.toJson(allProperties);
            record.setAllProperties(propertiesJson);
            // 同时也保存到 nodeVars 字段作为节点变量快照
            record.setNodeVars(propertiesJson);
            log.info("保存节点属性: nodeId={}, properties={}", nodeId, allProperties);
        }

        if (isNew) {
            save(record);
        } else {
            updateById(record);
        }

        log.info("节点就绪记录完成: nodeId={}, requiredPeople={}", nodeId, record.getRequiredPeople());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordNodeStart(String runId, String nodeId) {
        SimulationNodeRecord record = getRecordByRunIdAndNodeId(runId, nodeId);
        if (record == null) {
            log.warn("节点记录不存在，自动创建: runId={}, nodeId={}", runId, nodeId);
            record = new SimulationNodeRecord();
            record.setRunId(runId);
            record.setNodeId(nodeId);
            record.setExecutionOrder(++executionOrderCounter);
        }
        record.setExecutionStatus("running");
        record.setStartTime(LocalDateTime.now());

        if (record.getReadyTime() != null) {
            record.setWaitDurationMs(Duration.between(record.getReadyTime(), record.getStartTime()).toMillis());
        }
        updateById(record);
        log.debug("节点开始执行: runId={}, nodeId={}", runId, nodeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordNodeFinish(String runId, String nodeId, Map<String, Object> finalVars) {
        SimulationNodeRecord record = getRecordByRunIdAndNodeId(runId, nodeId);
        if (record == null) {
            log.warn("节点记录不存在，无法完成: runId={}, nodeId={}", runId, nodeId);
            return;
        }

        record.setExecutionStatus("done");
        record.setFinishTime(LocalDateTime.now());

        // 计算执行时间和总时间
        if (record.getStartTime() != null) {
            record.setExecDurationMs(Duration.between(record.getStartTime(), record.getFinishTime()).toMillis());
        }
        if (record.getReadyTime() != null) {
            record.setTotalDurationMs(Duration.between(record.getReadyTime(), record.getFinishTime()).toMillis());
        }

        // 保存最终变量（如果提供了）
        if (finalVars != null && !finalVars.isEmpty()) {
            // 合并现有变量和最终变量
            Map<String, Object> mergedVars = new HashMap<>();
            if (record.getNodeVars() != null) {
                Map<String, Object> existingVars = JsonUtils.parseToMap(record.getNodeVars());
                if (existingVars != null) {
                    mergedVars.putAll(existingVars);
                }
            }
            // 最终变量覆盖
            mergedVars.putAll(finalVars);
            // 标记为最终状态
            mergedVars.put("_final", true);
            mergedVars.put("_finishTime", record.getFinishTime().toString());

            String varsJson = JsonUtils.toJson(mergedVars);
            record.setNodeVars(varsJson);
            log.info("保存节点最终变量: nodeId={}, vars={}", nodeId, finalVars);
        }

        updateById(record);
        log.debug("节点完成: runId={}, nodeId={}, 耗时={}ms", runId, nodeId, record.getTotalDurationMs());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordNodeFinish(String runId, String nodeId) {
        recordNodeFinish(runId, nodeId, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordNodeWaiting(String runId, String nodeId, Integer queuePosition, Integer queueSize) {
        SimulationNodeRecord record = getRecordByRunIdAndNodeId(runId, nodeId);
        if (record == null) {
            record = new SimulationNodeRecord();
            record.setRunId(runId);
            record.setNodeId(nodeId);
            record.setExecutionOrder(++executionOrderCounter);
        }
        record.setExecutionStatus("waiting");
        record.setQueuePosition(queuePosition);
        record.setQueueSize(queueSize);
        updateById(record);
        log.debug("节点进入等待队列: runId={}, nodeId={}, position={}/{}", runId, nodeId, queuePosition, queueSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordNodeError(String runId, String nodeId, String errorMessage) {
        SimulationNodeRecord record = getRecordByRunIdAndNodeId(runId, nodeId);
        if (record == null) {
            record = new SimulationNodeRecord();
            record.setRunId(runId);
            record.setNodeId(nodeId);
            record.setExecutionOrder(++executionOrderCounter);
        }
        record.setExecutionStatus("error");
        record.setErrorMessage(errorMessage);
        record.setFinishTime(LocalDateTime.now());
        updateById(record);
        log.error("节点执行错误: runId={}, nodeId={}, error={}", runId, nodeId, errorMessage);
    }

    @Override
    public void updateNodeStatus(String runId, String nodeId, String status) {
        baseMapper.updateStatus(runId, nodeId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNodeVars(String runId, String nodeId, Map<String, Object> vars) {
        SimulationNodeRecord record = getRecordByRunIdAndNodeId(runId, nodeId);
        if (record == null) {
            log.warn("节点记录不存在: runId={}, nodeId={}", runId, nodeId);
            return;
        }

        // 合并现有变量
        Map<String, Object> mergedVars = new HashMap<>();
        if (record.getNodeVars() != null) {
            Map<String, Object> existingVars = JsonUtils.parseToMap(record.getNodeVars());
            if (existingVars != null) {
                mergedVars.putAll(existingVars);
            }
        }
        mergedVars.putAll(vars);

        String varsJson = JsonUtils.toJson(mergedVars);
        record.setNodeVars(varsJson);
        updateById(record);
        log.info("更新节点变量: nodeId={}", nodeId);
    }

    @Override
    public Map<String, Object> getNodeVars(String runId, String nodeId) {
        SimulationNodeRecord record = getRecordByRunIdAndNodeId(runId, nodeId);
        if (record == null || record.getNodeVars() == null) {
            return null;
        }

        return JsonUtils.parseToMap(record.getNodeVars());
    }

    @Override
    public Map<String, Object> getNodeStatistics(String runId, String nodeId) {
        SimulationNodeRecord record = getRecordByRunIdAndNodeId(runId, nodeId);
        Map<String, Object> stats = new HashMap<>();

        if (record != null) {
            stats.put("recordId", record.getRecordId());
            stats.put("nodeId", record.getNodeId());
            stats.put("nodeName", record.getNodeName());
            stats.put("nodeType", record.getNodeType());
            stats.put("status", record.getExecutionStatus());
            stats.put("readyTime", record.getReadyTime());
            stats.put("startTime", record.getStartTime());
            stats.put("finishTime", record.getFinishTime());
            stats.put("waitDurationMs", record.getWaitDurationMs());
            stats.put("execDurationMs", record.getExecDurationMs());
            stats.put("totalDurationMs", record.getTotalDurationMs());
            stats.put("requiredPeople", record.getRequiredPeople());
            stats.put("assignedPeople", record.getAssignedPeople());
            stats.put("queuePosition", record.getQueuePosition());
            stats.put("executionOrder", record.getExecutionOrder());
            stats.put("isStartNode", record.getIsStartNode());
            stats.put("isEndNode", record.getIsEndNode());
            stats.put("errorMessage", record.getErrorMessage());
            stats.put("stereotypeName", record.getStereotypeName());

            // 解析变量
            if (record.getNodeVars() != null) {
                Map<String, Object> vars = JsonUtils.parseToMap(record.getNodeVars());
                stats.put("nodeVars", vars != null ? vars : record.getNodeVars());
            }
        }

        return stats;
    }

    @Override
    public Map<String, Object> getRunStatistics(String runId) {
        List<SimulationNodeRecord> allNodes = getRecordsByRunId(runId);
        Long avgExecTime = getAvgExecutionTime(runId);
        Integer completedCount = getCompletedCount(runId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalNodes", allNodes.size());
        stats.put("completedNodes", completedCount);
        stats.put("runningNodes", allNodes.stream().filter(n -> "running".equals(n.getExecutionStatus())).count());
        stats.put("waitingNodes", allNodes.stream().filter(n -> "waiting".equals(n.getExecutionStatus())).count());
        stats.put("errorNodes", allNodes.stream().filter(n -> "error".equals(n.getExecutionStatus())).count());
        stats.put("readyNodes", allNodes.stream().filter(n -> "ready".equals(n.getExecutionStatus())).count());
        stats.put("avgExecutionTimeMs", avgExecTime != null ? avgExecTime : 0);

        if (!allNodes.isEmpty()) {
            LocalDateTime firstReady = allNodes.stream()
                    .filter(n -> n.getReadyTime() != null)
                    .map(SimulationNodeRecord::getReadyTime)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);
            LocalDateTime lastFinish = allNodes.stream()
                    .filter(n -> n.getFinishTime() != null)
                    .map(SimulationNodeRecord::getFinishTime)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            if (firstReady != null && lastFinish != null) {
                stats.put("totalRunDurationMs", Duration.between(firstReady, lastFinish).toMillis());
            }
        }

        // 收集所有节点的变量
        Map<String, Map<String, Object>> nodeVarsMap = new HashMap<>();
        for (SimulationNodeRecord record : allNodes) {
            if (record.getNodeVars() != null) {
                Map<String, Object> vars = JsonUtils.parseToMap(record.getNodeVars());
                if (vars != null) {
                    nodeVarsMap.put(record.getNodeName() != null ? record.getNodeName() : record.getNodeId(), vars);
                }
            }
        }
        stats.put("nodeVariables", nodeVarsMap);

        return stats;
    }
}