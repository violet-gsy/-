package com.jc.controller;

import com.jc.entity.SimulationRun;
import com.jc.service.SimulationRunService;
import com.jc.util.ApiResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/simulationRun")
@Slf4j
@Tag(name = "仿真运行记录")
public class SimulationRunController extends BaseController<SimulationRunService, SimulationRun> {

    public SimulationRunController(SimulationRunService service) {
        super(service);
    }

    // ========== 新增：重写分页查询，支持 diagramName ==========
    @Override
    @GetMapping("/page")
    @Operation(summary = "分页查询仿真运行记录")
    public ApiResponse<Page<SimulationRun>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String diagramId,
            @RequestParam(required = false) String diagramName,
            @RequestParam(required = false) String status) {

        Page<SimulationRun> page = new Page<>(current, size);
        LambdaQueryWrapper<SimulationRun> wrapper = new LambdaQueryWrapper<>();

        // 支持按 diagramId 查询
        if (StringUtils.hasText(diagramId)) {
            wrapper.eq(SimulationRun::getDiagramId, diagramId);
        }

        // 支持按 diagramName 查询
        if (StringUtils.hasText(diagramName)) {
            wrapper.like(SimulationRun::getDiagramName, diagramName);
        }

        // 支持按状态查询
        if (StringUtils.hasText(status)) {
            wrapper.eq(SimulationRun::getStatus, status);
        }

        // 按创建时间倒序
        wrapper.orderByDesc(SimulationRun::getCreatetime);

        service.page(page, wrapper);
        return ApiResponse.success(page);
    }

    // ========== 以下方法保持不变 ==========

    @PostMapping("/start")
    @Operation(summary = "开始仿真")
    public ApiResponse<SimulationRun> start(@RequestBody SimulationRun entity) {
        // 提取初始化变量
        Map<String, Object> initVars = entity.getInitVarsMap();
        entity.setInitVarsMap(null); // 防止重复序列化

        SimulationRun run = service.startSimulation(entity, initVars);
        return ApiResponse.success(run);
    }

    @Operation(summary = "结束仿真")
    @PostMapping("/end")
    public ApiResponse<Boolean> endSimulation(@RequestParam String id) {
        SimulationRun run = service.getById(id);
        if (run == null) {
            return ApiResponse.error("仿真记录不存在");
        }
        run.setEndTime(LocalDateTime.now());
        run.setStatus("ended");
        run.setDuration(Duration.between(run.getStartTime(), run.getEndTime()).toMillis());
        log.info("仿真结束，运行ID: {}, 持续时间: {}ms", id, run.getDuration());
        return ApiResponse.success(service.updateById(run));
    }

    @Operation(summary = "停止仿真")
    @PostMapping("/stop")
    public ApiResponse<Boolean> stopSimulation(@RequestParam String id) {
        SimulationRun run = service.getById(id);
        if (run == null) {
            return ApiResponse.error("仿真记录不存在");
        }
        run.setEndTime(LocalDateTime.now());
        run.setStatus("stopped");
        run.setDuration(Duration.between(run.getStartTime(), run.getEndTime()).toMillis());
        log.info("仿真停止，运行ID: {}", id);
        return ApiResponse.success(service.updateById(run));
    }

    @Operation(summary = "查询最近的运行记录")
    @GetMapping("/latest")
    public ApiResponse<SimulationRun> getLatestRun(@RequestParam String diagramId) {
        SimulationRun run = service.lambdaQuery()
                .eq(SimulationRun::getDiagramId, diagramId)
                .orderByDesc(SimulationRun::getStartTime)
                .last("LIMIT 1")
                .one();
        if (run == null) {
            return ApiResponse.error("未找到运行记录");
        }
        return ApiResponse.success(run);
    }

    @Operation(summary = "查询运行中的仿真")
    @GetMapping("/running")
    public ApiResponse<SimulationRun> getRunningRun(@RequestParam(required = false) String diagramId) {
        SimulationRun run = service.lambdaQuery()
                .eq(SimulationRun::getStatus, "running")
                .eq(diagramId != null, SimulationRun::getDiagramId, diagramId)
                .orderByDesc(SimulationRun::getStartTime)
                .last("LIMIT 1")
                .one();
        if (run == null) {
            return ApiResponse.error("没有运行中的仿真");
        }
        return ApiResponse.success(run);
    }

    @Operation(summary = "获取仿真初始化变量")
    @GetMapping("/initVars/{runId}")
    public ApiResponse<Map<String, Object>> getInitVars(@PathVariable String runId) {
        Map<String, Object> vars = service.getInitVars(runId);
        if (vars == null) {
            return ApiResponse.error("未找到初始化变量");
        }
        return ApiResponse.success(vars);
    }

    @Operation(summary = "更新仿真初始化变量")
    @PostMapping("/initVars/{runId}")
    public ApiResponse<Boolean> updateInitVars(
            @PathVariable String runId,
            @RequestBody Map<String, Object> initVars) {
        service.updateInitVars(runId, initVars);
        return ApiResponse.success(true);
    }
}