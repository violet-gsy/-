package com.jc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jc.entity.ActivityDiagram;
import com.jc.entity.LaunchMode;
import com.jc.service.ActivityDiagramService;
import com.jc.service.LaunchModeService;
import com.jc.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/activity-diagram")
@Slf4j
@Tag(name = "活动图管理")
public class ActivityDiagramController extends BaseController<ActivityDiagramService, ActivityDiagram> {

    @Autowired
    private LaunchModeService launchModeService;

    public ActivityDiagramController(ActivityDiagramService service) {
        super(service);
    }

    /**
     * 重写分页查询，填充测发模式名称
     */
    @Override
    @GetMapping("/page")
    @Operation(summary = "分页查询", description = "支持条件分页")
    public ApiResponse<Page<ActivityDiagram>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String subsystem) {

        Page<ActivityDiagram> page = new Page<>(current, size);
        LambdaQueryWrapper<ActivityDiagram> wrapper = new LambdaQueryWrapper<>();

        if (name != null && !name.isEmpty()) {
            wrapper.like(ActivityDiagram::getActivityDiagramName, name);
        }
        wrapper.orderByDesc(ActivityDiagram::getUpdateTime);

        service.page(page, wrapper);

        // 填充测发模式名称
        page.getRecords().forEach(diagram -> {
            if (diagram.getLaunchModeId() != null) {
                LaunchMode mode = launchModeService.getById(diagram.getLaunchModeId());
                if (mode != null) {
                    diagram.setLaunchModeName(mode.getLaunchModeName());
                }
            }
        });

        return ApiResponse.success(page);
    }

    /**
     * 重写根据ID查询，填充测发模式名称
     */
    @Override
    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID查询")
    public ApiResponse<ActivityDiagram> getById(@PathVariable String id) {
        ActivityDiagram diagram = service.getById(id);
        if (diagram == null) {
            return ApiResponse.error("未找到该记录");
        }
        if (diagram.getLaunchModeId() != null) {
            LaunchMode mode = launchModeService.getById(diagram.getLaunchModeId());
            if (mode != null) {
                diagram.setLaunchModeName(mode.getLaunchModeName());
            }
        }
        return ApiResponse.success(diagram);
    }

    /**
     * 重写保存，处理测发模式关联
     */
    @Override
    @PostMapping("/save")
    @Operation(summary = "新增数据")
    public ApiResponse<Boolean> save(@RequestBody ActivityDiagram entity) {
        try {
            // 处理测发模式关联
            handleLaunchMode(entity);
            return ApiResponse.success(service.save(entity));
        } catch (Exception e) {
            log.error("保存失败", e);
            return ApiResponse.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 重写更新，处理测发模式关联
     */
    @Override
    @PutMapping("/update")
    @Operation(summary = "修改数据")
    public ApiResponse<Boolean> update(@RequestBody ActivityDiagram entity) {
        try {
            // 处理测发模式关联
            handleLaunchMode(entity);
            return ApiResponse.success(service.updateById(entity));
        } catch (Exception e) {
            log.error("更新失败", e);
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 更新流程的测发模式（使用模式ID）
     */
    /**
     * 更新流程的测发模式 - 先按名称查找
     */
    @PutMapping("/update-launch-mode")
    @Operation(summary = "更新流程的测发模式")
    public ApiResponse<Boolean> updateLaunchMode(
            @RequestParam String diagramId,
            @RequestParam(required = false) String modeId,
            @RequestParam(required = false) String diagramName) {
        try {
            ActivityDiagram diagram = null;

            // 1. 先根据ID查找
            if (diagramId != null && !diagramId.isEmpty()) {
                diagram = service.getById(diagramId);
            }

            // 2. 如果ID没找到，根据名称查找
            if (diagram == null && diagramName != null && !diagramName.isEmpty()) {
                LambdaQueryWrapper<ActivityDiagram> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(ActivityDiagram::getActivityDiagramName, diagramName);
                diagram = service.getOne(wrapper);
            }

            // 3. 如果还是没找到，返回错误
            if (diagram == null) {
                return ApiResponse.error("未找到该流程，请先同步");
            }

            // 更新测发模式ID
            diagram.setLaunchModeId(modeId);
            // 更新时间
            diagram.setUpdateTime(LocalDateTime.now());

            boolean result = service.updateById(diagram);
            if (result) {
                return ApiResponse.success(true);
            } else {
                return ApiResponse.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新测发模式失败", e);
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }
    /**
     * 批量更新流程的测发模式
     */
    @PutMapping("/batch-update-launch-mode")
    @Operation(summary = "批量更新流程的测发模式")
    public ApiResponse<Boolean> batchUpdateLaunchMode(
            @RequestParam String modeId,
            @RequestBody List<String> diagramIds) {
        try {
            for (String diagramId : diagramIds) {
                ActivityDiagram diagram = service.getById(diagramId);
                if (diagram != null) {
                    diagram.setLaunchModeId(modeId);
                    service.updateById(diagram);
                }
            }
            return ApiResponse.success(true);
        } catch (Exception e) {
            log.error("批量更新测发模式失败", e);
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 处理测发模式关联
     */
    private void handleLaunchMode(ActivityDiagram entity) {
        // 如果有测发模式名称，查找对应的ID
        if (entity.getLaunchModeName() != null && !entity.getLaunchModeName().isEmpty()) {
            LambdaQueryWrapper<LaunchMode> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LaunchMode::getLaunchModeName, entity.getLaunchModeName())
                    .eq(LaunchMode::getStatus, "1");
            LaunchMode mode = launchModeService.getOne(wrapper);
            if (mode != null) {
                entity.setLaunchModeId(mode.getLaunchModeId());
            }
        } else if (entity.getLaunchModeId() != null && !entity.getLaunchModeId().isEmpty()) {
            // 如果有ID但没有名称，验证ID是否存在
            LaunchMode mode = launchModeService.getById(entity.getLaunchModeId());
            if (mode == null || !"1".equals(mode.getStatus())) {
                entity.setLaunchModeId(null);
            }
        } else {
            // 如果都没有，清空关联
            entity.setLaunchModeId(null);
        }
    }
}