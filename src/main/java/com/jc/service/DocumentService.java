package com.jc.service;

import com.jc.entity.Document;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jc.util.ApiResponse;
import com.jc.vo.SaveDocVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
* @author Lenovo
* @description 针对表【T_DOCUMENT(文档主表)】的数据库操作Service
* @createDate 2026-05-12 17:34:15
*/
public interface DocumentService extends BaseService<Document> {

    ApiResponse savedoc(SaveDocVo entity, MultipartFile file);

    void download(String id, HttpServletResponse response);
}
