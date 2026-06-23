package com.jc.controller;

import com.jc.entity.Launchsite;
import com.jc.entity.TInterface;
import com.jc.service.LaunchsiteService;
import com.jc.service.TInterfaceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/interface")
@Slf4j
@Tag(name = "接口关联类")
public class InterFaceController extends BaseController<TInterfaceService, TInterface> {
    public InterFaceController(TInterfaceService service) {
        super(service);
    }
    // 自带全部CRUD 继承basecontroller
}
