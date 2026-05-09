package com.jc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.entity.Task;
import com.jc.service.TaskService;
import com.jc.mapper.TaskMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【T_TASK(T_TASK)】的数据库操作Service实现
* @createDate 2026-05-07 17:44:04
*/
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task>
    implements TaskService{

}




