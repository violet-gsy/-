package com.jc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jc.entity.Document;
import com.jc.entity.DocumentHis;
import com.jc.mapper.DocumentHisMapper;
import com.jc.service.DocumentService;
import com.jc.mapper.DocumentMapper;
import com.jc.util.ApiResponse;
import com.jc.util.DmUuidUtil;
import com.jc.vo.SaveDocVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Resource
    DocumentHisMapper documentHisMapper;

    // 读取配置文件中的上传路径
    @Value("${file.uploadPath}")
    private String uploadPath;

    @Override
    public ApiResponse savedoc(SaveDocVo input,MultipartFile file) {
        String filename = file.getOriginalFilename();
        String uuid = DmUuidUtil.get32Uuid();
        String path = uploadPath + File.separator;
        String saveFilename = uuid+filename;
        try {
            // 1. 判断文件是否为空
            if (file.isEmpty()) {
                return ApiResponse.error("上传文件不能为空");
            }
            // 2. 创建存储目录，不存在则创建
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // 完整保存路径
            File saveFile = new File(path + File.separator + saveFilename);
            // 写入磁盘
            file.transferTo(saveFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Document doc = new Document();
        doc.setId(uuid);
        doc.setName(input.getName());
        doc.setType(input.getType());
        doc.setFilesize(file.getSize());
        doc.setFilepath(path + File.separator + saveFilename);
        doc.setVersion("v" + 1.0);
        doc.setCreator(input.getCreator());
        doc.setRemark(input.getRemark());
        doc.setSubsystem(input.getSubsystem());
        documentMapper.insert(doc);
        return ApiResponse.success("文件信息保存成功");
}

    @Override
    public void download(String filepath, HttpServletResponse response) {
        /*Document doc = documentMapper.selectById(id);
        if (doc == null) {
            try {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain;charset=utf-8");
                response.getWriter().write("文档记录不存在");
                response.getWriter().flush();
            } catch (IOException e) {
                // 忽略
            }
            return;
        }*/

        File targetFile = new File(filepath);
        String filename = targetFile.getName();
        if (!targetFile.exists()) {
            try {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain;charset=utf-8");
                response.getWriter().write("文件不存在");
                response.getWriter().flush();
            } catch (IOException e) {
                // 忽略
            }
            return;
        }

        try {
            // 下载响应头
            response.setContentType("application/octet-stream");
            String encodeName = URLEncoder.encode(filename.substring(32,filename.length()), "UTF-8");
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
            try {
                response.reset(); // 尝试重置响应，但可能已经提交
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("text/plain;charset=utf-8");
                response.getWriter().write("文件下载失败：" + e.getMessage());
                response.getWriter().flush();
            } catch (IOException ex) {
                // 忽略
            }
        }
    }

    @Override
    public ApiResponse updateFile(String id, MultipartFile file) {
        Document doc = documentMapper.selectById(id);
        String filename = file.getOriginalFilename();
        String uuid = DmUuidUtil.get32Uuid();
        String path = uploadPath + File.separator;
        String saveFilename = uuid+filename;
        try {
            // 1. 判断文件是否为空
            if (file.isEmpty()) {
                return ApiResponse.error("上传文件不能为空");
            }
            // 2. 创建存储目录，不存在则创建
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // 完整保存路径
            File saveFile = new File(path + File.separator + saveFilename);
            // 写入磁盘
            file.transferTo(saveFile);
        } catch (Exception e) {
            e.printStackTrace();
        }



        //将当前版本存入历史表
        DocumentHis his = new DocumentHis();
        his.setDocid(doc.getId());
        his.setHisdocname(doc.getName());
        his.setVersion(doc.getVersion());
        his.setHisfilepath(doc.getFilepath());
        his.setHisfilesize(doc.getFilesize());
        documentHisMapper.insert(his);

        //修改文档库
        String version = doc.getVersion();
        double num = Double.parseDouble(version.substring(1,version.length())) + 1;
        doc.setFilepath(path + File.separator + saveFilename);
        doc.setFilesize(file.getSize());
        doc.setVersion("v" + num);
        doc.setUpdatetime(LocalDateTime.now());
        return ApiResponse.success(documentMapper.updateById(doc));
    }

    @Override
    public List<Map> getAllVersion(String id) {
        List<Map> result = new ArrayList<>();
        Document doc = documentMapper.selectById(id);
        Map docmap = new HashMap();
        docmap.put("id",doc.getId());
        docmap.put("version",doc.getVersion());
        docmap.put("filepath",doc.getFilepath());
        docmap.put("type","最新版本");
        result.add(docmap);

        List<DocumentHis> hisList = documentHisMapper.selectList(new QueryWrapper<DocumentHis>().eq("DOCID",id).orderByDesc("CREATETIME"));
        for (DocumentHis documentHis : hisList) {
            Map hismap = new HashMap();
            hismap.put("id",documentHis.getId());
            hismap.put("version",documentHis.getVersion());
            hismap.put("filepath",documentHis.getHisfilepath());
            hismap.put("type","历史版本");
            result.add(hismap);
        }
        return result;
    }
}




