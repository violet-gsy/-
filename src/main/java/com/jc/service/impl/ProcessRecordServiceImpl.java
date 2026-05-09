package com.jc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.entity.ProcessRecord;
import com.jc.service.ProcessRecordService;
import com.jc.mapper.ProcessRecordMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【T_PROCESS_RECORD(流程记录结果表)】的数据库操作Service实现
* @createDate 2026-05-07 17:44:04
*/
@Service
public class ProcessRecordServiceImpl extends ServiceImpl<ProcessRecordMapper, ProcessRecord>
    implements ProcessRecordService{

}




