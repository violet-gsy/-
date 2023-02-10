package com.cetc28.needmanagement;

import com.cetc28.needmanagement.entity.CompanyDct;
import com.cetc28.needmanagement.service.CompanyDctService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@SpringBootApplication
@MapperScan("com.cetc28.needmanagement.mapper")
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class);




    }
}
