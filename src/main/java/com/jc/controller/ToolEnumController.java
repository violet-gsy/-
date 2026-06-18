package com.jc.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jc.allenum.*;
import com.jc.entity.*;
import com.jc.service.TableColumnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.jc.util.ApiResponse;
import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/toolEnum")
@Slf4j
@Tag(name = "工具枚举类")
public class ToolEnumController {

    @Resource
    TableColumnService tableColumnService;

    @Operation(summary = "/获取所有后端接口路由")
    @GetMapping("/getAllRoute")
    public ApiResponse<List<Map<String, Object>>> getAllRoute() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ToolEnum enums : ToolEnum.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", enums.getCode());
            map.put("desc", enums.getDesc());
            list.add(map);
        }
        return ApiResponse.success(list);
    }

    @Operation(summary = "/获取表头")
    @GetMapping("/getTableHead")
    public ApiResponse<List<TableColumn>> getTableHead(@RequestParam String code) {
        return ApiResponse.success(tableColumnService.list(new QueryWrapper<TableColumn>().eq("CODE",code)));
    }

    @Operation(summary = "/获取所有枚举菜单项")
    @GetMapping("/getAllEnum")
    public ApiResponse<Map> getAllEnum(@RequestParam String tableCode) {
        Map result = new HashMap();
        //状态
        List<Map<String, Object>> status = new ArrayList<>();
        //类型
        List<Map<String, Object>> type = new ArrayList<>();
        //相态
        List<Map<String, Object>> phase = new ArrayList<>();
        //危险等级
        List<Map<String, Object>> hazardlevel = new ArrayList<>();
        if (tableCode.equals(ToolEnum.LAUNCHSITE.getCode())){
            //发射场表
        }else if (tableCode.equals(ToolEnum.LSAREA.getCode())){
            //发射场区域
        }else if (tableCode.equals(ToolEnum.LSKEYFACILITY.getCode())){
            //发射场设施
            for (FacilityTypeEnum enums : FacilityTypeEnum.values()) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", enums.getCode());
                map.put("desc", enums.getDesc());
                type.add(map);
            }
            for (StatusEnum enums : StatusEnum.values()) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", enums.getCode());
                map.put("desc", enums.getDesc());
                status.add(map);
            }
        }else if (tableCode.equals(ToolEnum.PRODUCT.getCode())){
            //产品
            for (ProductTypeEnum enums : ProductTypeEnum.values()) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", enums.getCode());
                map.put("desc", enums.getDesc());
                type.add(map);
            }
            for (StatusEnum enums : StatusEnum.values()) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", enums.getCode());
                map.put("desc", enums.getDesc());
                status.add(map);
            }
        }else if (tableCode.equals(ToolEnum.PRODUCTCOMPONENT.getCode())){
            //产品组件
            for (ProductCptTypeEnum enums : ProductCptTypeEnum.values()) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", enums.getCode());
                map.put("desc", enums.getDesc());
                type.add(map);
            }
        }else if (tableCode.equals(ToolEnum.LSEQUIPMENT.getCode())){
            //设备
            for (EquipmentTypeEnum enums : EquipmentTypeEnum.values()) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", enums.getCode());
                map.put("desc", enums.getDesc());
                type.add(map);
            }
            for (StatusEnum enums : StatusEnum.values()) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", enums.getCode());
                map.put("desc", enums.getDesc());
                status.add(map);
            }
        }else if (tableCode.equals(ToolEnum.TOOLFIXTRUE.getCode())){
            //工装
            for (ToolFixtrueTypeEnum enums : ToolFixtrueTypeEnum.values()) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", enums.getCode());
                map.put("desc", enums.getDesc());
                type.add(map);
            }
            for (StatusEnum enums : StatusEnum.values()) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", enums.getCode());
                map.put("desc", enums.getDesc());
                status.add(map);
            }
        }else if (tableCode.equals(ToolEnum.TOOLEQUIPMENT.getCode())){
            //工器具
            for (ToolEquipmentTypeEnum enums : ToolEquipmentTypeEnum.values()) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", enums.getCode());
                map.put("desc", enums.getDesc());
                type.add(map);
            }
            for (StatusEnum enums : StatusEnum.values()) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", enums.getCode());
                map.put("desc", enums.getDesc());
                status.add(map);
            }
        }else if (tableCode.equals(ToolEnum.MAJOR.getCode())){
            //专业
            Map<String, Object> map = new HashMap<>();
            map.put("code", "001");
            map.put("desc", "已启用");
            status.add(map);
            Map<String, Object> map1 = new HashMap<>();
            map1.put("code", "002");
            map1.put("desc", "已禁用");
            status.add(map1);
        }else if (tableCode.equals(ToolEnum.PERSONNEL.getCode())){
            //人员
            Map<String, Object> map = new HashMap<>();
            map.put("code", "001");
            map.put("desc", "工作中");
            status.add(map);
            Map<String, Object> map1 = new HashMap<>();
            map1.put("code", "002");
            map1.put("desc", "未工作");
            status.add(map1);

            List<Map<String, Object>> sex = new ArrayList<>();

            Map<String, Object> mapsex = new HashMap<>();
            mapsex.put("code", "001");
            mapsex.put("desc", "男");
            sex.add(mapsex);
            Map<String, Object> mapsex1 = new HashMap<>();
            mapsex1.put("code", "002");
            mapsex1.put("desc", "女");
            sex.add(mapsex1);
            result.put("sex",sex);

        }else if (tableCode.equals(ToolEnum.LAUNCHSITEFLUID.getCode())){
            //发射场流体介质
            for (FluidTypeEnum enums : FluidTypeEnum.values()) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", enums.getCode());
                map.put("desc", enums.getDesc());
                type.add(map);
            }
            for (PhaseTypeEnum enums : PhaseTypeEnum.values()) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", enums.getCode());
                map.put("desc", enums.getDesc());
                phase.add(map);
            }
            for (HzdLevelEnum enums : HzdLevelEnum.values()) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", enums.getCode());
                map.put("desc", enums.getDesc());
                hazardlevel.add(map);
            }
        }/*else if (tableCode.equals(ToolEnum.DOCUMENT.getCode())){
            //文档
            for (DocumentTypeEnum enums : DocumentTypeEnum.values()) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", enums.getCode());
                map.put("desc", enums.getDesc());
                type.add(map);
            }
        }*/

        result.put("status",status);
        result.put("type",type);
        result.put("phase",phase);
        result.put("hazardlevel",hazardlevel);
        return ApiResponse.success(result);
    }


}
