package com.jc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.entity.Document;
import com.jc.service.DocumentService;
import com.jc.mapper.DocumentMapper;
import com.jc.util.ApiResponse;
import com.jc.vo.SaveDocVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
* @author Lenovo
* @description 针对表【T_DOCUMENT(文档主表)】的数据库操作Service实现
* @createDate 2026-05-12 17:34:15
*/
@Service
public class DocumentServiceImpl extends BaseServiceImpl<DocumentMapper, Document>
    implements DocumentService{

    @Resource
    DocumentMapper documentMapper;

    // 读取配置文件中的上传路径
    @Value("${file.uploadPath}")
    private String uploadPath;

    @Override
    public ApiResponse savedoc(SaveDocVo input) {
        MultipartFile file = input.getFile();
        String filename = file.getOriginalFilename();


        try {
            // 1. 判断文件是否为空
            if (file.isEmpty()) {
                return ApiResponse.error("上传文件不能为空");
            }
            // 2. 创建存储目录，不存在则创建
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // 完整保存路径
            File saveFile = new File(uploadPath + File.separator + filename);
            // 写入磁盘
            file.transferTo(saveFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Document doc = new Document();
        doc.setName(filename);
        doc.setType(input.getType());
        doc.setFilesize(file.getSize());
        doc.setFilepath(uploadPath + File.separator + filename);
        doc.setVersion(input.getVersion());
        doc.setCreator(input.getCreator());
        doc.setRemark(input.getRemark());
        documentMapper.insert(doc);
        return ApiResponse.success("文件信息保存成功");
    }

    @Override
    public ApiResponse download(String fileName, HttpServletResponse response) {
        try {
            File targetFile = new File(uploadPath + File.separator + fileName);
            if (!targetFile.exists()) {
                response.setContentType("text/plain;charset=utf-8");
                response.getWriter().write("文件不存在");
                return ApiResponse.error("文件不存在");
            }
            // 下载响应头
            response.setContentType("application/octet-stream");
            String encodeName = URLEncoder.encode(fileName, "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + encodeName);

            // 流输出下载
            try (FileInputStream fis = new FileInputStream(targetFile);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ApiResponse.success("下载成功");
    }
}




