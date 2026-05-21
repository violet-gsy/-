package com.jc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.entity.Product;
import com.jc.service.ProductService;
import com.jc.mapper.ProductMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【T_PRODUCT(产品表)】的数据库操作Service实现
* @createDate 2026-05-12 17:34:15
*/
@Service
public class ProductServiceImpl extends BaseServiceImpl<ProductMapper, Product>
    implements ProductService{

}




