package com.jc.controller;

import com.jc.entity.SimulationNodeRecord;
import com.jc.entity.SimulationNodeVarRecord;
import com.jc.service.SimulationNodeRecordService;
import com.jc.service.SimulationNodeVarRecordService;
import com.jc.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/simulationNode")
@Slf4j
@Tag(name = "仿真节点记录管理")
public class SimulationNodeController {

    @Autowired
    private SimulationNodeRecordService nodeRecordService;

    @Autowired
    private SimulationNodeVarRecordService varRecordService;

    @Operation(summary = "获取某次仿真的所有节点记录")
    @GetMapping("/records/{runId}")
    public ApiResponse<List<SimulationNodeRecord>> getNodeRecords(
            @Parameter(description = "仿真运行ID", required = true)
            @PathVariable String runId) {
        return ApiResponse.success(nodeRecordService.getRecordsByRunId(runId));
    }

    @Operation(summary = "获取运行中的节点")
    @GetMapping("/records/running/{runId}")
    public ApiResponse<List<SimulationNodeRecord>> getRunningNodes(
            @Parameter(description = "仿真运行ID", required = true)
            @PathVariable String runId) {
        return ApiResponse.success(nodeRecordService.getRunningNodes(runId));
    }

    @Operation(summary = "获取等待中的节点")
    @GetMapping("/records/waiting/{runId}")
    public ApiResponse<List<SimulationNodeRecord>> getWaitingNodes(
            @Parameter(description = "仿真运行ID", required = true)
            @PathVariable String runId) {
        return ApiResponse.success(nodeRecordService.getWaitingNodes(runId));
    }

    @Operation(summary = "获取已完成的节点")
    @GetMapping("/records/completed/{runId}")
    public ApiResponse<List<SimulationNodeRecord>> getCompletedNodes(
            @Parameter(description = "仿真运行ID", required = true)
            @PathVariable String runId) {
        return ApiResponse.success(nodeRecordService.getCompletedNodes(runId));
    }

    @Operation(summary = "获取单个节点的运行记录")
    @GetMapping("/record/{runId}/{nodeId}")
    public ApiResponse<SimulationNodeRecord> getNodeRecord(
            @Parameter(description = "仿真运行ID", required = true)
            @PathVariable String runId,
            @Parameter(description = "节点ID", required = true)
            @PathVariable String nodeId) {
        return ApiResponse.success(nodeRecordService.getRecordByRunIdAndNodeId(runId, nodeId));
    }

    @Operation(summary = "获取节点变量")
    @GetMapping("/vars/{runId}/{nodeId}")
    public ApiResponse<Map<String, Object>> getNodeVars(
            @Parameter(description = "仿真运行ID", required = true)
            @PathVariable String runId,
            @Parameter(description = "节点ID", required = true)
            @PathVariable String nodeId) {
        Map<String, Object> vars = nodeRecordService.getNodeVars(runId, nodeId);
        if (vars == null) {
            return ApiResponse.error("未找到节点变量");
        }
        return ApiResponse.success(vars);
    }

    @Operation(summary = "更新节点变量")
    @PostMapping("/vars/{runId}/{nodeId}")
    public ApiResponse<Boolean> updateNodeVars(
            @Parameter(description = "仿真运行ID", required = true)
            @PathVariable String runId,
            @Parameter(description = "节点ID", required = true)
            @PathVariable String nodeId,
            @RequestBody Map<String, Object> vars) {
        nodeRecordService.updateNodeVars(runId, nodeId, vars);
        return ApiResponse.success(true);
    }

    @Operation(summary = "获取节点变量记录（兼容旧接口）")
    @GetMapping("/vars/records/{runId}/{nodeId}")
    public ApiResponse<List<SimulationNodeVarRecord>> getNodeVarRecords(
            @Parameter(description = "仿真运行ID", required = true)
            @PathVariable String runId,
            @Parameter(description = "节点ID", required = true)
            @PathVariable String nodeId) {
        return ApiResponse.success(varRecordService.getVarsByRunIdAndNodeId(runId, nodeId));
    }

    @Operation(summary = "获取节点统计信息")
    @GetMapping("/stats/node/{runId}/{nodeId}")
    public ApiResponse<Map<String, Object>> getNodeStats(
            @Parameter(description = "仿真运行ID", required = true)
            @PathVariable String runId,
            @Parameter(description = "节点ID", required = true)
            @PathVariable String nodeId) {
        return ApiResponse.success(nodeRecordService.getNodeStatistics(runId, nodeId));
    }

    @Operation(summary = "获取仿真运行整体统计")
    @GetMapping("/stats/run/{runId}")
    public ApiResponse<Map<String, Object>> getRunStats(
            @Parameter(description = "仿真运行ID", required = true)
            @PathVariable String runId) {
        return ApiResponse.success(nodeRecordService.getRunStatistics(runId));
    }

    @Operation(summary = "记录节点就绪（包含所有变量）")
    @PostMapping("/record/ready")
    public ApiResponse<Boolean> recordNodeReady(
            @RequestParam String runId,
            @RequestParam String nodeId,
            @RequestParam String nodeName,
            @RequestParam(required = false) String nodeType,
            @RequestParam(required = false) Integer requiredPeople,
            @RequestParam(required = false) Boolean isStartNode,
            @RequestParam(required = false) Boolean isEndNode,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String stereotypeName,
            @RequestBody(required = false) Map<String, Object> allProperties) {

        log.info("记录节点就绪: runId={}, nodeId={}, nodeName={}, requiredPeople={}, stereotypeName={}, properties={}",
                runId, nodeId, nodeName, requiredPeople, stereotypeName, allProperties);

        nodeRecordService.recordNodeReady(
                runId, nodeId, nodeName, nodeType,
                requiredPeople, isStartNode, isEndNode,
                description, allProperties, stereotypeName);
        return ApiResponse.success(true);
    }

    @Operation(summary = "记录节点开始执行")
    @PostMapping("/record/start")
    public ApiResponse<Boolean> recordNodeStart(
            @RequestParam String runId,
            @RequestParam String nodeId) {
        nodeRecordService.recordNodeStart(runId, nodeId);
        return ApiResponse.success(true);
    }

    @Operation(summary = "记录节点完成（带最终变量）")
    @PostMapping("/record/finish")
    public ApiResponse<Boolean> recordNodeFinish(
            @RequestParam String runId,
            @RequestParam String nodeId,
            @RequestBody(required = false) Map<String, Object> finalVars) {
        nodeRecordService.recordNodeFinish(runId, nodeId, finalVars);
        return ApiResponse.success(true);
    }

    @Operation(summary = "记录节点等待")
    @PostMapping("/record/waiting")
    public ApiResponse<Boolean> recordNodeWaiting(
            @RequestParam String runId,
            @RequestParam String nodeId,
            @RequestParam Integer queuePosition,
            @RequestParam Integer queueSize) {
        nodeRecordService.recordNodeWaiting(runId, nodeId, queuePosition, queueSize);
        return ApiResponse.success(true);
    }

    @Operation(summary = "记录节点错误")
    @PostMapping("/record/error")
    public ApiResponse<Boolean> recordNodeError(
            @RequestParam String runId,
            @RequestParam String nodeId,
            @RequestParam String errorMessage) {
        nodeRecordService.recordNodeError(runId, nodeId, errorMessage);
        return ApiResponse.success(true);
    }
}