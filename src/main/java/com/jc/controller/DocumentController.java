package com.jc.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jc.allenum.DocumentTypeEnum;
import com.jc.allenum.ProductTypeEnum;
import com.jc.allenum.StatusEnum;
import com.jc.entity.Document;
import com.jc.entity.Product;
import com.jc.service.DocumentService;
import com.jc.service.ProductService;
import com.jc.util.ApiResponse;
import com.jc.vo.SaveDocVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/document")
@Slf4j
@Tag(name = "文档类")
public class DocumentController  {

    @Resource
    DocumentService documentService;

    @Operation(summary = "新增数据", description = "通用新增接口")
    @PostMapping("/save")
    public ApiResponse save(
            @Parameter(description = "实体数据", required = true)
            @RequestBody SaveDocVo entity) {
        ApiResponse response = documentService.savedoc(entity);
        return response;
    }

    @Operation(summary = "分页查询", description = "支持条件分页")
    @GetMapping("/page")
    public ApiResponse<Page<Document>> page(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页条数", example = "10")
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "查询条件")
                    Document query) {
        // 分页对象必须用 Integer 构造！！！
        Page<Document> page = new Page<>(current, size);
        QueryWrapper<Document> wrapper = new QueryWrapper<>(query);
        documentService.page(page, wrapper);

        return ApiResponse.success(page);
    }

    /**
     * 文件下载接口，跨域可用
     */
    @Operation(summary = "文件下载")
    @GetMapping("/download")
    public ApiResponse download(@RequestParam String fileName, HttpServletResponse response) {
        return documentService.download(fileName,response);
    }



}