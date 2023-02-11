package com.cetc28.needmanagement.controller;

import com.alibaba.excel.EasyExcel;
import com.cetc28.needmanagement.entity.UserNeed;
import com.cetc28.needmanagement.service.UserNeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/userNeed")
public class UserNeedController {


    @Autowired
    UserNeedService userNeedService;

    @GetMapping("/export")
    public void export(HttpServletResponse response){
        List<UserNeed> userNeedList = userNeedService.list();
        String fileName = "用户需求.xlsx";
        Set<String> excludeColumnFieldNames = new HashSet<>();
        excludeColumnFieldNames.add("projectName");
        EasyExcel.write(fileName, UserNeed.class).sheet("模板").doWrite(userNeedList);
    }

    @GetMapping("/importUserNeed")
    public void importUserNeed(HttpServletResponse response) throws FileNotFoundException {
        String fileName = "C:\\Users\\aaa\\Desktop\\用户需求.xlsx";
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        // 这里每次会读取100条数据 然后返回过来 直接调用使用数据就行
       /* EasyExcel.read(fileName, UserNeed.class, new commonListener(dataList -> {
            //每100行会触发一次此方法
            System.out.println(dataList);

        })).sheet().doRead();*/
        InputStream inputStream = new FileInputStream(fileName);
        List<UserNeed> userNeedList = EasyExcel.read(inputStream)
                .head(UserNeed.class)
                .sheet()
                .headRowNumber(1)
                .doReadSync();
        for(int i=0;i<userNeedList.size();i++){
            userNeedList.get(i).setNEED_ID("666");
            userNeedService.saveOrUpdate(userNeedList.get(i));
        }

    }

}
