package com.jc.mapper;

import com.jc.entity.SimulationNodeRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SimulationNodeRecordMapper extends BaseMapper<SimulationNodeRecord> {

    @Select("SELECT * FROM CF_SIMULATION_NODE_RECORD WHERE RUN_ID = #{runId} ORDER BY EXECUTION_ORDER, CREATE_TIME")
    List<SimulationNodeRecord> selectByRunId(@Param("runId") String runId);

    @Select("SELECT * FROM CF_SIMULATION_NODE_RECORD WHERE RUN_ID = #{runId} AND NODE_ID = #{nodeId}")
    SimulationNodeRecord selectByRunIdAndNodeId(@Param("runId") String runId, @Param("nodeId") String nodeId);

    @Select("SELECT * FROM CF_SIMULATION_NODE_RECORD WHERE RUN_ID = #{runId} AND EXECUTION_STATUS = 'running'")
    List<SimulationNodeRecord> selectRunningNodes(@Param("runId") String runId);

    @Select("SELECT * FROM CF_SIMULATION_NODE_RECORD WHERE RUN_ID = #{runId} AND EXECUTION_STATUS = 'waiting' ORDER BY QUEUE_POSITION")
    List<SimulationNodeRecord> selectWaitingNodes(@Param("runId") String runId);

    @Select("SELECT * FROM CF_SIMULATION_NODE_RECORD WHERE RUN_ID = #{runId} AND EXECUTION_STATUS IN ('done', 'error') ORDER BY FINISH_TIME")
    List<SimulationNodeRecord> selectCompletedNodes(@Param("runId") String runId);

    @Select("SELECT * FROM CF_SIMULATION_NODE_RECORD WHERE RUN_ID = #{runId} AND NODE_NAME = #{nodeName}")
    SimulationNodeRecord selectByRunIdAndNodeName(@Param("runId") String runId, @Param("nodeName") String nodeName);

    @Select("SELECT COUNT(*) FROM CF_SIMULATION_NODE_RECORD WHERE RUN_ID = #{runId} AND EXECUTION_STATUS = 'done'")
    Integer countCompletedNodes(@Param("runId") String runId);

    @Select("SELECT AVG(TOTAL_DURATION_MS) FROM CF_SIMULATION_NODE_RECORD WHERE RUN_ID = #{runId} AND EXECUTION_STATUS = 'done'")
    Long selectAvgExecutionTime(@Param("runId") String runId);

    @Select("SELECT MAX(EXECUTION_ORDER) FROM CF_SIMULATION_NODE_RECORD WHERE RUN_ID = #{runId}")
    Integer selectMaxExecutionOrder(@Param("runId") String runId);

    @Update("UPDATE CF_SIMULATION_NODE_RECORD SET EXECUTION_STATUS = #{status}, UPDATE_TIME = CURRENT_TIMESTAMP WHERE RUN_ID = #{runId} AND NODE_ID = #{nodeId}")
    int updateStatus(@Param("runId") String runId, @Param("nodeId") String nodeId, @Param("status") String status);
}