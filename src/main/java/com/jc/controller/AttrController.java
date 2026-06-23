package com.jc.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jc.allenum.ToolEnum;
import com.jc.entity.*;
import com.jc.service.*;
import com.jc.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Resource
    TRelationService tRelationService;

    @Resource
    LsareaService lsareaService;

    @Resource
    LaunchsiteService launchsiteService;

    @Resource
    LsKeyfacilityService lsKeyfacilityService;

    @Resource
    ProductService productService;

    @Resource
    ProductComponentService productComponentService;

    @Resource
    LsEquipmentService lsEquipmentService;

    @Resource
    ToolFixtureService toolFixtureService;

    @Resource
    ToolEquipmentService toolEquipmentService;

    @Resource
    MajorService majorService;

    @Resource
    PersonnelService personnelService;

    @Resource
    LaunchSiteFluidService launchSiteFluidService;

    @Resource
    DocumentService documentService;

    @PostMapping("/saveRelationList")
    @Operation(summary = "批量保存关联关系")
    public ApiResponse saveRelationList(@RequestBody List<TRelation> relations) {
        return ApiResponse.success("成功",tRelationService.saveBatch(relations));
    }

    @PostMapping("/selRelationList")
    @Operation(summary = "查询关联表数据")
    public ApiResponse selRelationList(@RequestParam String ourid) {
        List<TRelation> relations = tRelationService.list(new QueryWrapper<TRelation>().eq("OURID",ourid));
        for (TRelation relation : relations) {
            if (relation.getTargetbstype().equals(ToolEnum.LAUNCHSITE.getCode())){
                //发射场表
                Launchsite launchsite = launchsiteService.getById(relation.getTargetid());
                relation.setTargetname(launchsite.getName());
            }else if (relation.getTargetbstype().equals(ToolEnum.LSAREA.getCode())){
                //发射场区域
                Lsarea lsarea = lsareaService.getById(relation.getTargetid());
                relation.setTargetname(lsarea.getName());
            }else if (relation.getTargetbstype().equals(ToolEnum.LSKEYFACILITY.getCode())){
                //发射场设施
                LsKeyfacility lsKeyfacility = lsKeyfacilityService.getById(relation.getTargetid());
                relation.setTargetname(lsKeyfacility.getName());
            }else if (relation.getTargetbstype().equals(ToolEnum.PRODUCT.getCode())){
                //产品
                Product byId = productService.getById(relation.getTargetid());
                relation.setTargetname(byId.getName());
            }else if (relation.getTargetbstype().equals(ToolEnum.PRODUCTCOMPONENT.getCode())){
                //产品组件
                ProductComponent byId = productComponentService.getById(relation.getTargetid());
                relation.setTargetname(byId.getName());
            }else if (relation.getTargetbstype().equals(ToolEnum.LSEQUIPMENT.getCode())){
                //设备
                LsEquipment byId = lsEquipmentService.getById(relation.getTargetid());
                relation.setTargetname(byId.getName());
            }else if (relation.getTargetbstype().equals(ToolEnum.TOOLFIXTRUE.getCode())){
                //工装
                ToolFixture byId = toolFixtureService.getById(relation.getTargetid());
                relation.setTargetname(byId.getName());
            }else if (relation.getTargetbstype().equals(ToolEnum.TOOLEQUIPMENT.getCode())){
                //工器具
                ToolEquipment byId = toolEquipmentService.getById(relation.getTargetid());
                relation.setTargetname(byId.getName());
            }else if (relation.getTargetbstype().equals(ToolEnum.MAJOR.getCode())){
                //专业
                Major byId = majorService.getById(relation.getTargetid());
                relation.setTargetname(byId.getName());
            }else if (relation.getTargetbstype().equals(ToolEnum.PERSONNEL.getCode())){
                //人员
                Personnel byId = personnelService.getById(relation.getTargetid());
                relation.setTargetname(byId.getName());
            }else if (relation.getTargetbstype().equals(ToolEnum.LAUNCHSITEFLUID.getCode())){
                //发射场流体介质
                LaunchSiteFluid byId = launchSiteFluidService.getById(relation.getTargetid());
                relation.setTargetname(byId.getName());
            }/*else if (relation.getTargetbstype().equals(ToolEnum.DOCUMENT.getCode())){
                //文档
                Document byId = documentService.getById(relation.getTargetid());
                relation.setTargetname(byId.getDocname());
            }*/else {
                log.info("------------后端人员注意："+relation.getTargetbstype()+"暂无该目标表类型-------------");
            }
        }
        Map<String, List<TRelation>> groupByClass = relations.stream()
                .collect(Collectors.groupingBy(TRelation::getTargetbstype));
        return ApiResponse.success("成功",groupByClass);
    }

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
