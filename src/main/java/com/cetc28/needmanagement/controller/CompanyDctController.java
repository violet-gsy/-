package com.cetc28.needmanagement.controller;

import com.cetc28.needmanagement.entity.CompanyDct;
import com.cetc28.needmanagement.service.CompanyDctService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/companyDct")
public class CompanyDctController {
    @Autowired
    CompanyDctService companyDctService;


    @GetMapping("/selCompanyDct")
    public List<CompanyDct> selCompanyDct(){

        return companyDctService.list();
    }

    @GetMapping("/count")
    public Long selCompanyDct1(){
        return 5L;
//        return companyDctService.count();
    }

}
