package com.jc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.entity.TableColumn;
import com.jc.service.TableColumnService;
import com.jc.mapper.TableColumnMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【T_TABLE_COLUMN(自定义表头)】的数据库操作Service实现
* @createDate 2026-05-27 15:02:25
*/
@Service
public class TableColumnServiceImpl extends ServiceImpl<TableColumnMapper, TableColumn>
    implements TableColumnService{

}




