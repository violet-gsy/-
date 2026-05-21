package com.jc.controller;

import com.jc.entity.ProductComponent;
import com.jc.service.ProductComponentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/productComponent")
@Slf4j
@Tag(name = "产品组件类")
public class ProductComponentController extends BaseController<ProductComponentService, ProductComponent> {
    public ProductComponentController(ProductComponentService service) {
        super(service);
    }

    // 自带全部CRUD 继承basecontroller
}