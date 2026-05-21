package com.jc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.entity.Document;
import com.jc.service.DocumentService;
import com.jc.mapper.DocumentMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【T_DOCUMENT(文档主表)】的数据库操作Service实现
* @createDate 2026-05-12 17:34:15
*/
@Service
public class DocumentServiceImpl extends BaseServiceImpl<DocumentMapper, Document>
    implements DocumentService{

}




