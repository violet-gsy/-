package com.jc.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jc.entity.TableColumn;
import com.jc.service.TableColumnService;
import com.jc.util.ApiResponse;
import com.jc.util.ToolEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
