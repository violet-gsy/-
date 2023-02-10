package com.cetc28.needmanagement.controller;

import com.cetc28.needmanagement.entity.CompanyDct;
import com.cetc28.needmanagement.service.CompanyDctService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/companyDct")
public class CompanyDctController {
    @Autowired
    CompanyDctService companyDctService;


    @RequestMapping("/selCompanyDct")
    public List<CompanyDct> selCompanyDct(){
        return companyDctService.list();
    }

}
