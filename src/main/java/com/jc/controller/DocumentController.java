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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @PostMapping(value="/save",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse save(
            @RequestParam String type,
            @RequestParam String remark,
            @RequestParam String creator,
            @RequestParam MultipartFile file) {
        SaveDocVo entity = SaveDocVo.builder()
                .type(type)
                .remark(remark)
                .creator(creator)
                .build();
        ApiResponse response = documentService.savedoc(entity,file);
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
    @GetMapping(value = "/download")
    public void  download(@RequestParam String id, HttpServletResponse response) {
         documentService.download(id,response);
    }

    @Operation(summary = "删除")
    @DeleteMapping(value = "/delete")
    public ApiResponse<Boolean>  download(@RequestParam String id) {
        return ApiResponse.success(documentService.removeById(id));
    }

    @Operation(summary = "修改数据", description = "根据ID修改")
    @PutMapping("/updateInfo")
    public ApiResponse<Boolean> updateInfo(
            @Parameter(description = "修改后的实体", required = true)
            @RequestBody Document entity) {
        return ApiResponse.success(documentService.updateById(entity));
    }



}