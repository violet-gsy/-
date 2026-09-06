package com.jc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jc.entity.LaunchMode;
import com.jc.entity.LaunchModeDiagram;
import com.jc.service.LaunchModeService;
import com.jc.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/launchMode")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "测发模式管理")
public class LaunchModeController {

    private final LaunchModeService launchModeService;

    @GetMapping("/list")
    @Operation(summary = "获取测发模式列表")
    public ApiResponse<List<LaunchMode>> list(@RequestParam(required = false) String status) {
        LambdaQueryWrapper<LaunchMode> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(LaunchMode::getStatus, status);
        }
        wrapper.orderByAsc(LaunchMode::getSortOrder);
        return ApiResponse.success(launchModeService.list(wrapper));
    }

    @GetMapping("/page")
    @Operation(summary = "分页获取测发模式")
    public ApiResponse<IPage<LaunchMode>> page(@RequestParam(defaultValue = "1") Integer current,
                                               @RequestParam(defaultValue = "10") Integer size,
                                               @RequestParam(required = false) String keyword) {
        Page<LaunchMode> page = new Page<>(current, size);
        LambdaQueryWrapper<LaunchMode> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(LaunchMode::getLaunchModeName, keyword)
                    .or()
                    .like(LaunchMode::getLaunchModeCode, keyword);
        }
        wrapper.orderByAsc(LaunchMode::getSortOrder);
        return ApiResponse.success(launchModeService.page(page, wrapper));
    }

    @GetMapping("/enabled")
    @Operation(summary = "获取所有启用的测发模式")
    public ApiResponse<List<LaunchMode>> getEnabled() {
        return ApiResponse.success(launchModeService.getEnabledLaunchModes());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取测发模式详情（含关联流程）")
    public ApiResponse<LaunchMode> getDetail(@PathVariable String id) {
        return ApiResponse.success(launchModeService.getLaunchModeWithDiagrams(id));
    }

    @GetMapping("/{id}/diagrams")
    @Operation(summary = "获取测发模式下的流程列表")
    public ApiResponse<List<LaunchModeDiagram>> getDiagrams(@PathVariable String id) {
        return ApiResponse.success(launchModeService.getDiagramsByLaunchMode(id));
    }

    @PostMapping("/save")
    @Operation(summary = "新增测发模式")
    public ApiResponse<Boolean> save(@RequestBody LaunchMode launchMode) {
        return ApiResponse.success(launchModeService.save(launchMode));
    }

    @PostMapping("/saveWithDiagrams")
    @Operation(summary = "新增测发模式并关联流程")
    public ApiResponse<Boolean> saveWithDiagrams(@RequestBody LaunchModeSaveDTO dto) {
        return ApiResponse.success(launchModeService.saveLaunchModeWithDiagrams(dto.getLaunchMode(), dto.getDiagramIds()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新测发模式")
    public ApiResponse<Boolean> update(@RequestBody LaunchMode launchMode) {
        return ApiResponse.success(launchModeService.updateById(launchMode));
    }

    @PutMapping("/{id}/diagrams")
    @Operation(summary = "更新测发模式的流程关联")
    public ApiResponse<Boolean> updateDiagrams(@PathVariable String id, @RequestBody List<String> diagramIds) {
        return ApiResponse.success(launchModeService.updateLaunchModeDiagrams(id, diagramIds));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除测发模式")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        return ApiResponse.success(launchModeService.removeById(id));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "切换测发模式状态")
    public ApiResponse<Boolean> toggleStatus(@PathVariable String id) {
        LaunchMode launchMode = launchModeService.getById(id);
        if (launchMode == null) {
            return ApiResponse.error("测发模式不存在");
        }
        launchMode.setStatus("1".equals(launchMode.getStatus()) ? "0" : "1");
        return ApiResponse.success(launchModeService.updateById(launchMode));
    }
}