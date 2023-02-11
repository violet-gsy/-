package com.cetc28.needmanagement.controller;

import com.cetc28.needmanagement.entity.CompanyDct;
import com.cetc28.needmanagement.service.CompanyDctService;
import org.springframework.beans.factory.annotation.Autowired;
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

        List<CompanyDct> list = companyDctService.list();
        return list;
    }


    @GetMapping("/selCompanyDct1")
    public CompanyDct selCompanyDct1(){
        String COMPANY_CODE = "TEXT1";
        return companyDctService.getById(COMPANY_CODE);
    }

    @GetMapping("/saveCompanyDct")
    public boolean saveCompanyDct(){
        String COMPANY_CODE = "TEXT2";
        CompanyDct companyDct=new CompanyDct();
        companyDct.setCOMPANY_CODE(COMPANY_CODE);
        return companyDctService.saveOrUpdate(companyDct);
    }
}
