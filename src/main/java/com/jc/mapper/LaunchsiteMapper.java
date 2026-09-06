package com.jc.mapper;

import com.jc.entity.Launchsite;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【T_LAUNCHSITE(发射场表)】的数据库操作Mapper
* @createDate 2026-05-12 17:34:15
* @Entity com.jc.entity.Launchsite
*/
public interface LaunchsiteMapper extends BaseMapper<Launchsite> {

    // 查询所有发射场【本级编码】01、02
    @Select("SELECT code FROM T_LAUNCHSITE WHERE code IS NOT NULL AND code != ''")
    List<String> selectAllCode();

    // 根据ID查本级编码
    @Select("SELECT code FROM T_LAUNCHSITE WHERE id = #{treeId}")
    String selectCodeByTreeId(@Param("treeId") String treeId);

}




