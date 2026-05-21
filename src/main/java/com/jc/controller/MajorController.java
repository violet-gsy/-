package com.jc.controller;

import com.jc.entity.Major;
import com.jc.service.MajorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/major")
@Slf4j
@Tag(name = "专业类")
public class MajorController extends BaseController<MajorService, Major> {
    public MajorController(MajorService service) {
        super(service);
    }
    // 自带全部CRUD 继承basecontroller
}