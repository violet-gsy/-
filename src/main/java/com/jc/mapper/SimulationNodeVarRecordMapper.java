package com.jc.mapper;

import com.jc.entity.SimulationNodeVarRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SimulationNodeVarRecordMapper extends BaseMapper<SimulationNodeVarRecord> {

    @Select("SELECT * FROM CF_SIMULATION_NODE_VAR_RECORD WHERE RUN_ID = #{runId} AND NODE_ID = #{nodeId} ORDER BY SEQUENCE_NO")
    List<SimulationNodeVarRecord> selectByRunIdAndNodeId(@Param("runId") String runId, @Param("nodeId") String nodeId);

    @Select("SELECT * FROM CF_SIMULATION_NODE_VAR_RECORD WHERE RUN_ID = #{runId} AND NODE_ID = #{nodeId} AND IS_FINAL = 1")
    List<SimulationNodeVarRecord> selectFinalVars(@Param("runId") String runId, @Param("nodeId") String nodeId);

    @Select("SELECT * FROM CF_SIMULATION_NODE_VAR_RECORD WHERE RUN_ID = #{runId} AND NODE_ID = #{nodeId} AND IS_INITIAL = 1")
    List<SimulationNodeVarRecord> selectInitialVars(@Param("runId") String runId, @Param("nodeId") String nodeId);

    @Select("SELECT * FROM CF_SIMULATION_NODE_VAR_RECORD WHERE RUN_ID = #{runId} AND VAR_NAME = #{varName} ORDER BY SEQUENCE_NO DESC LIMIT 1")
    SimulationNodeVarRecord selectLatestVarByRunIdAndName(@Param("runId") String runId, @Param("varName") String varName);

    @Select("SELECT DISTINCT VAR_NAME FROM CF_SIMULATION_NODE_VAR_RECORD WHERE RUN_ID = #{runId}")
    List<String> selectDistinctVarNames(@Param("runId") String runId);

    @Select("SELECT * FROM CF_SIMULATION_NODE_VAR_RECORD WHERE RUN_ID = #{runId} AND NODE_ID = #{nodeId} AND IS_OUTPUT = 1 AND IS_FINAL = 1")
    List<SimulationNodeVarRecord> selectFinalOutputVars(@Param("runId") String runId, @Param("nodeId") String nodeId);
}