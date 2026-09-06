package com.jc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.entity.ActivityDiagram;
import com.jc.mapper.ActivityDiagramMapper;
import com.jc.service.ActivityDiagramService;
import org.springframework.stereotype.Service;

@Service
public class ActivityDiagramServiceImpl
        extends ServiceImpl<ActivityDiagramMapper, ActivityDiagram>
        implements ActivityDiagramService {
    // 无需额外实现，继承即可获得完整CRUD
}