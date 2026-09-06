package com.jc.mapper;

import com.jc.entity.SimulationRun;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SimulationRunMapper extends BaseMapper<SimulationRun> {

    @Select("SELECT * FROM T_SIMULATION_RUN WHERE DIAGRAM_ID = #{diagramId} ORDER BY START_TIME DESC LIMIT 10")
    List<SimulationRun> selectLatestByDiagramId(String diagramId);
}