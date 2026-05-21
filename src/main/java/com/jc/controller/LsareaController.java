package com.jc.controller;

import com.jc.entity.LaunchSiteFluid;
import com.jc.entity.Lsarea;
import com.jc.service.LaunchSiteFluidService;
import com.jc.service.LsareaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lsarea")
@Slf4j
@Tag(name = "发射场区域类")
public class LsareaController extends BaseController<LsareaService, Lsarea> {
    public LsareaController(LsareaService service) {
        super(service);
    }

    // 自带全部CRUD 继承basecontroller
}
