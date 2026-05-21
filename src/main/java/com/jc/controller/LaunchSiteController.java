package com.jc.controller;

import com.jc.entity.Launchsite;
import com.jc.service.LaunchsiteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/launchSite")
@Slf4j
@Tag(name = "发射场类")
public class LaunchSiteController extends BaseController<LaunchsiteService, Launchsite> {
    public LaunchSiteController(LaunchsiteService service) {
        super(service);
    }
    // 自带全部CRUD 继承basecontroller
}
