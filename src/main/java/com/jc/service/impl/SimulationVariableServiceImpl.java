package com.jc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.entity.SimulationVariable;
import com.jc.mapper.SimulationVariableMapper;
import com.jc.service.SimulationVariableService;
import org.springframework.stereotype.Service;

@Service
public class SimulationVariableServiceImpl extends ServiceImpl<SimulationVariableMapper, SimulationVariable>
        implements SimulationVariableService {
}