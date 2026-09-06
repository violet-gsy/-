package com.jc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.entity.SimulationRun;
import com.jc.mapper.SimulationRunMapper;
import com.jc.service.SimulationRunService;
import com.jc.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
public class SimulationRunServiceImpl extends ServiceImpl<SimulationRunMapper, SimulationRun>
        implements SimulationRunService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SimulationRun startSimulation(SimulationRun entity, Map<String, Object> initVars) {
        entity.setStartTime(LocalDateTime.now());
        entity.setStatus("running");

        // 保存初始化变量
        if (initVars != null && !initVars.isEmpty()) {
            String initVarsJson = JsonUtils.toJson(initVars);
            entity.setInitVars(initVarsJson);
            log.info("保存初始化变量: {}", initVars);
        }

        save(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInitVars(String runId, Map<String, Object> initVars) {
        SimulationRun run = getById(runId);
        if (run == null) {
            log.warn("仿真记录不存在: runId={}", runId);
            return;
        }

        String initVarsJson = JsonUtils.toJson(initVars);
        run.setInitVars(initVarsJson);
        updateById(run);
        log.info("更新仿真初始化变量: runId={}", runId);
    }

    @Override
    public Map<String, Object> getInitVars(String runId) {
        SimulationRun run = getById(runId);
        if (run == null || run.getInitVars() == null) {
            return null;
        }

        return JsonUtils.parseToMap(run.getInitVars());
    }
}