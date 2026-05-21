package com.jc.controller;

import com.jc.entity.Product;
import com.jc.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product")
@Slf4j
@Tag(name = "产品类")
public class ProductController extends BaseController<ProductService, Product> {
    public ProductController(ProductService service) {
        super(service);
    }


    // 自带全部CRUD 继承basecontroller
}