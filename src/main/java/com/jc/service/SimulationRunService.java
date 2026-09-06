package com.jc.service;

import com.jc.entity.SimulationRun;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface SimulationRunService extends IService<SimulationRun> {

    /**
     * 开始仿真并保存初始化变量
     */
    SimulationRun startSimulation(SimulationRun entity, Map<String, Object> initVars);

    /**
     * 更新仿真初始化变量
     */
    void updateInitVars(String runId, Map<String, Object> initVars);

    /**
     * 获取仿真初始化变量
     */
    Map<String, Object> getInitVars(String runId);
}