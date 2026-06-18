package com.jc.controller;

import com.jc.allenum.StatusEnum;
import com.jc.allenum.ToolEquipmentTypeEnum;
import com.jc.allenum.ToolFixtrueTypeEnum;
import com.jc.entity.ToolEquipment;
import com.jc.service.ToolEquipmentService;
import com.jc.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/toolEquipment")
@Slf4j
@Tag(name = "工器具类")
public class ToolEquipmentController extends BaseController<ToolEquipmentService, ToolEquipment> {
    public ToolEquipmentController(ToolEquipmentService service) {
        super(service);
    }
    // 自带全部CRUD 继承basecontroller
}