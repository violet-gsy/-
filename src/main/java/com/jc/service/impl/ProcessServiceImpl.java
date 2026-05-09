package com.jc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.entity.Process;
import com.jc.service.ProcessService;
import com.jc.mapper.ProcessMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【T_PROCESS(业务流程表)】的数据库操作Service实现
* @createDate 2026-05-07 17:44:04
*/
@Service
public class ProcessServiceImpl extends ServiceImpl<ProcessMapper, Process>
    implements ProcessService{

}




