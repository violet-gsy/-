package com.jc.mapper;

import com.jc.entity.Attr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【T_ATTR】的数据库操作Mapper
* @createDate 2026-05-12 17:34:15
* @Entity com.jc.entity.Attr
*/
public interface AttrMapper extends BaseMapper<Attr> {


    List<Attr> getAttr(String attrid);
}




