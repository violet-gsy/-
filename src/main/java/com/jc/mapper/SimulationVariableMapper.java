package com.jc.mapper;

import com.jc.entity.SimulationVariable;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SimulationVariableMapper extends BaseMapper<SimulationVariable> {

    @Select("SELECT * FROM T_SIMULATION_VARIABLE WHERE RUN_ID = #{runId} ORDER BY TIMESTAMP, SEQUENCE_NO")
    List<SimulationVariable> selectByRunId(String runId);
}