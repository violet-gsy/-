package com.jc.controller;

import com.jc.entity.SimulationVariable;
import com.jc.service.SimulationVariableService;
import com.jc.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/simulationVariable")
@Slf4j
@Tag(name = "仿真变量")
public class SimulationVariableController extends BaseController<SimulationVariableService, SimulationVariable> {

    public SimulationVariableController(SimulationVariableService service) {
        super(service);
    }

    @Operation(summary = "批量保存变量")
    @PostMapping("/batchSave")
    public ApiResponse<Boolean> batchSave(@RequestBody List<SimulationVariable> variables) {
        if (variables == null || variables.isEmpty()) {
            return ApiResponse.success("没有数据需要保存");
        }
        log.info("批量保存变量，数量: {}", variables.size());
        return ApiResponse.success(service.saveBatch(variables));
    }

    @Operation(summary = "根据运行ID查询变量")
    @GetMapping("/listByRunId")
    public ApiResponse<List<SimulationVariable>> listByRunId(@RequestParam String runId) {
        List<SimulationVariable> variables = service.lambdaQuery()
                .eq(SimulationVariable::getRunId, runId)
                .orderByAsc(SimulationVariable::getTimestamp)
                .orderByAsc(SimulationVariable::getSequenceNo)
                .list();
        return ApiResponse.success(variables);
    }

    @Operation(summary = "根据运行ID和变量名查询")
    @GetMapping("/getByName")
    public ApiResponse<SimulationVariable> getByName(@RequestParam String runId, @RequestParam String variableName) {
        SimulationVariable variable = service.lambdaQuery()
                .eq(SimulationVariable::getRunId, runId)
                .eq(SimulationVariable::getVariableName, variableName)
                .orderByDesc(SimulationVariable::getTimestamp)
                .last("LIMIT 1")
                .one();
        if (variable == null) {
            return ApiResponse.error("未找到该变量");
        }
        return ApiResponse.success(variable);
    }
}