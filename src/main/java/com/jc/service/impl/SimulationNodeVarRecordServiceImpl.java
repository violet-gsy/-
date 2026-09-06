package com.jc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.entity.SimulationNodeVarRecord;
import com.jc.mapper.SimulationNodeVarRecordMapper;
import com.jc.service.SimulationNodeVarRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SimulationNodeVarRecordServiceImpl extends ServiceImpl<SimulationNodeVarRecordMapper, SimulationNodeVarRecord>
        implements SimulationNodeVarRecordService {

    private static int sequenceCounter = 0;

    @Override
    public List<SimulationNodeVarRecord> getVarsByRunIdAndNodeId(String runId, String nodeId) {
        return baseMapper.selectByRunIdAndNodeId(runId, nodeId);
    }

    @Override
    public List<SimulationNodeVarRecord> getFinalVars(String runId, String nodeId) {
        return baseMapper.selectFinalVars(runId, nodeId);
    }

    @Override
    public List<SimulationNodeVarRecord> getInitialVars(String runId, String nodeId) {
        return baseMapper.selectInitialVars(runId, nodeId);
    }

    @Override
    public List<SimulationNodeVarRecord> getFinalOutputVars(String runId, String nodeId) {
        return baseMapper.selectFinalOutputVars(runId, nodeId);
    }

    @Override
    public SimulationNodeVarRecord getLatestVarByRunIdAndName(String runId, String varName) {
        return baseMapper.selectLatestVarByRunIdAndName(runId, varName);
    }

    @Override
    public List<String> getDistinctVarNames(String runId) {
        return baseMapper.selectDistinctVarNames(runId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordNodeVars(String runId, String nodeId, String nodeName,
                               Map<String, Object> values, Map<String, Boolean> inputOutputMap,
                               boolean isInitial, boolean isFinal) {
        if (values == null || values.isEmpty()) {
            return;
        }

        List<SimulationNodeVarRecord> records = new ArrayList<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String varName = entry.getKey();
            Object varValue = entry.getValue();

            SimulationNodeVarRecord record = new SimulationNodeVarRecord();
            record.setRunId(runId);
            record.setNodeId(nodeId);
            record.setNodeName(nodeName);
            record.setVarName(varName);
            record.setVarValue(varValue != null ? String.valueOf(varValue) : null);
            record.setVarType(detectValueType(varValue));
            record.setCaptureTime(LocalDateTime.now());
            record.setSequenceNo(++sequenceCounter);
            record.setIsInitial(isInitial);
            record.setIsFinal(isFinal);

            // 设置输入输出标识
            if (inputOutputMap != null && inputOutputMap.containsKey(varName)) {
                Boolean isInput = inputOutputMap.get(varName);
                if (isInput != null) {
                    record.setIsInput(isInput);
                    record.setIsOutput(!isInput);
                }
            } else {
                // 默认：如果是初始值且没有明确标记，默认为输入
                if (isInitial) {
                    record.setIsInput(true);
                    record.setIsOutput(false);
                } else if (isFinal) {
                    record.setIsInput(false);
                    record.setIsOutput(true);
                }
            }

            records.add(record);
        }

        saveBatch(records);
        log.debug("记录节点变量: runId={}, nodeId={}, varCount={}, isInitial={}, isFinal={}",
                runId, nodeId, records.size(), isInitial, isFinal);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordNodeVarsWithType(String runId, String nodeId, String nodeName,
                                       Map<String, Object> values, Map<String, String> varTypeMap,
                                       Map<String, Boolean> inputOutputMap,
                                       boolean isInitial, boolean isFinal) {
        if (values == null || values.isEmpty()) {
            return;
        }

        List<SimulationNodeVarRecord> records = new ArrayList<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String varName = entry.getKey();
            Object varValue = entry.getValue();

            SimulationNodeVarRecord record = new SimulationNodeVarRecord();
            record.setRunId(runId);
            record.setNodeId(nodeId);
            record.setNodeName(nodeName);
            record.setVarName(varName);
            record.setVarValue(varValue != null ? String.valueOf(varValue) : null);

            // 使用指定的类型，否则自动检测
            if (varTypeMap != null && varTypeMap.containsKey(varName)) {
                record.setVarType(varTypeMap.get(varName));
            } else {
                record.setVarType(detectValueType(varValue));
            }

            record.setCaptureTime(LocalDateTime.now());
            record.setSequenceNo(++sequenceCounter);
            record.setIsInitial(isInitial);
            record.setIsFinal(isFinal);

            if (inputOutputMap != null && inputOutputMap.containsKey(varName)) {
                Boolean isInput = inputOutputMap.get(varName);
                if (isInput != null) {
                    record.setIsInput(isInput);
                    record.setIsOutput(!isInput);
                }
            }

            records.add(record);
        }

        saveBatch(records);
    }

    private String detectValueType(Object value) {
        if (value == null) return "null";
        if (value instanceof Boolean) return "boolean";
        if (value instanceof Integer) return "integer";
        if (value instanceof Long) return "integer";
        if (value instanceof Double) return "number";
        if (value instanceof Float) return "number";
        if (value instanceof String) return "string";
        if (value instanceof List) return "array";
        if (value instanceof Map) return "object";
        return "object";
    }
}