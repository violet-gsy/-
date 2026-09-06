package com.jc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jc.entity.SimulationNodeVarRecord;
import java.util.List;
import java.util.Map;

public interface SimulationNodeVarRecordService extends IService<SimulationNodeVarRecord> {

    List<SimulationNodeVarRecord> getVarsByRunIdAndNodeId(String runId, String nodeId);

    List<SimulationNodeVarRecord> getFinalVars(String runId, String nodeId);

    List<SimulationNodeVarRecord> getInitialVars(String runId, String nodeId);

    List<SimulationNodeVarRecord> getFinalOutputVars(String runId, String nodeId);

    SimulationNodeVarRecord getLatestVarByRunIdAndName(String runId, String varName);

    List<String> getDistinctVarNames(String runId);

    void recordNodeVars(String runId, String nodeId, String nodeName, Map<String, Object> values,
                        Map<String, Boolean> inputOutputMap, boolean isInitial, boolean isFinal);

    void recordNodeVarsWithType(String runId, String nodeId, String nodeName, Map<String, Object> values,
                                Map<String, String> varTypeMap, Map<String, Boolean> inputOutputMap,
                                boolean isInitial, boolean isFinal);
}