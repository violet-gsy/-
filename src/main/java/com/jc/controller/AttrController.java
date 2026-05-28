package com.jc.controller;

import cn.hutool.core.util.StrUtil;
import com.jc.entity.Attr;
import com.jc.entity.AttrUnit;
import com.jc.entity.Datatype;
import com.jc.service.AttrService;
import com.jc.service.AttrUnitService;
import com.jc.service.DatatypeService;
import com.jc.util.ApiResponse;
import com.jc.vo.EmptyFlowVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.repository.Deployment;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/attr")
@Slf4j
@Tag(name = "属性及关联关系类")
public class AttrController {

    @Resource
    AttrService attrService;

    @Resource
    AttrUnitService attrUnitService;

    @Resource
    DatatypeService datatypeService;

    @GetMapping("/getAllAttrUnit")
    @Operation(summary = "获取计量单位列表")
    public ApiResponse<List<AttrUnit>> getAllAttrUnit(){
        return ApiResponse.success("成功",attrUnitService.list());
    }

    @GetMapping("/getAllDataType")
    @Operation(summary = "获取数据类型列表")
    public ApiResponse<List<Datatype>> getAllDataType(){
        return ApiResponse.success("成功",datatypeService.list());
    }

    @PostMapping("/saveOrUpdateAttr")
    @Operation(summary = "保存或修改私有属性")
    public ApiResponse saveOrUpdateAttr(@RequestBody Attr vo) {
        if (vo.getCreatetime() == null){
            vo.setCreatetime(LocalDateTime.now());
        }
        return ApiResponse.success("成功",attrService.saveOrUpdate(vo));
    }

    @GetMapping("/getAttr")
    @Operation(summary = "获取私有属性列表")
    public ApiResponse<List<Attr>> getAttr(@RequestParam String attrid){
        List<Attr> attrs = attrService.getAttr(attrid);
        return ApiResponse.success("成功",attrs);
    }

    @DeleteMapping("/delAttr")
    @Operation(summary = "删除私有属性")
    public ApiResponse<Boolean> delAttr(@RequestParam String id){
        return ApiResponse.success(attrService.removeById(id));
    }

    @PostMapping("/updateAttrList")
    @Operation(summary = "批量修改私有属性")
    public ApiResponse updateAttrList(@RequestBody List<Attr> attrs) {
        return ApiResponse.success("成功",attrService.updateBatchById(attrs));
    }
}
