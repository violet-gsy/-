package com.jc.controller;

import com.jc.allenum.FacilityTypeEnum;
import com.jc.allenum.StatusEnum;
import com.jc.allenum.ToolEnum;
import com.jc.entity.LsKeyfacility;
import com.jc.service.LsKeyfacilityService;
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
@RequestMapping("/lsKeyfacility")
@Slf4j
@Tag(name = "发射场关键设施类")
public class LsKeyfacilityController extends BaseController<LsKeyfacilityService, LsKeyfacility> {
    public LsKeyfacilityController(LsKeyfacilityService service) {
        super(service);
    }

    // 自带全部CRUD 继承basecontroller

}