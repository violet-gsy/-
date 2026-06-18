package com.jc.controller;

import com.jc.allenum.DocumentTypeEnum;
import com.jc.allenum.FluidTypeEnum;
import com.jc.allenum.HzdLevelEnum;
import com.jc.allenum.PhaseTypeEnum;
import com.jc.entity.LaunchSiteFluid;
import com.jc.service.LaunchSiteFluidService;
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
@RequestMapping("/launchSiteFluid")
@Slf4j
@Tag(name = "发射场流体介质表类")
public class LaunchSiteFluidController extends BaseController<LaunchSiteFluidService, LaunchSiteFluid> {
    public LaunchSiteFluidController(LaunchSiteFluidService service) {
        super(service);
    }
    // 自带全部CRUD 继承basecontroller

}
