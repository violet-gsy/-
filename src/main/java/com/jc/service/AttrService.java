package com.jc.service;

import com.jc.entity.Attr;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【T_ATTR】的数据库操作Service
* @createDate 2026-05-12 17:34:15
*/
public interface AttrService extends BaseService<Attr> {

    List<Attr> getAttr(String attrid);
}
