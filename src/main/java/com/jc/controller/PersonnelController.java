package com.jc.controller;

import com.jc.entity.Major;
import com.jc.entity.Personnel;
import com.jc.service.MajorService;
import com.jc.service.PersonnelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/personnel")
@Slf4j
@Tag(name = "人员类")
public class PersonnelController extends BaseController<PersonnelService, Personnel> {
    public PersonnelController(PersonnelService service) {
        super(service);
    }

    // 自带全部CRUD 继承basecontroller
}
