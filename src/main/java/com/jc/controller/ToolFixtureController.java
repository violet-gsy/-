package com.jc.controller;

import com.jc.allenum.EquipmentTypeEnum;
import com.jc.allenum.StatusEnum;
import com.jc.allenum.ToolFixtrueTypeEnum;
import com.jc.entity.ToolFixture;
import com.jc.service.ToolFixtureService;
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
@RequestMapping("/toolFixture")
@Slf4j
@Tag(name = "工装类")
public class ToolFixtureController extends BaseController<ToolFixtureService, ToolFixture> {
    public ToolFixtureController(ToolFixtureService service) {
        super(service);
    }

    // 自带全部CRUD 继承basecontroller

}
