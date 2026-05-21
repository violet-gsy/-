package com.jc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jc.service.BaseService;
import com.jc.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

public class BaseController<S extends BaseService<T>, T> {

    protected final S service;

    public BaseController(S service) {
        this.service = service;
    }

    @Operation(summary = "新增数据", description = "通用新增接口")
    @PostMapping("/save")
    public ApiResponse<Boolean> save(
            @Parameter(description = "实体数据", required = true)
            @RequestBody T entity) {
        return ApiResponse.success(service.save(entity));
    }

    @Operation(summary = "修改数据", description = "根据ID修改")
    @PutMapping("/update")
    public ApiResponse<Boolean> update(
            @Parameter(description = "修改后的实体", required = true)
            @RequestBody T entity) {
        return ApiResponse.success(service.updateById(entity));
    }

    @Operation(summary = "删除数据", description = "根据ID删除")
    @DeleteMapping("/remove/{id}")
    public ApiResponse<Boolean> remove(
            @Parameter(description = "主键ID", required = true)
            @PathVariable Long id) {
        return ApiResponse.success(service.removeById(id));
    }

    @Operation(summary = "根据ID查询", description = "单条详情")
    @GetMapping("/get/{id}")
    public ApiResponse<T> getById(
            @Parameter(description = "主键ID", required = true)
            @PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @Operation(summary = "分页查询", description = "支持条件分页")
    @GetMapping("/page")
    public ApiResponse<Page<T>> page(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页条数", example = "10")
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "查询条件")
            T query) {
        // 分页对象必须用 Integer 构造！！！
        Page<T> page = new Page<>(current, size);
        QueryWrapper<T> wrapper = new QueryWrapper<>(query);
        service.page(page, wrapper);

        return ApiResponse.success(page);
    }

    @Operation(summary = "查询列表", description = "条件查询全部数据")
    @GetMapping("/list")
    public ApiResponse<Object> list(
            @Parameter(description = "查询条件")
            T query) {
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>(query);
        return ApiResponse.success(service.list(wrapper));
    }
}
