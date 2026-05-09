package com.jc.service;

import com.jc.util.ApiResponse;
import com.jc.vo.FlowChartVO;
import com.jc.vo.JsonToFlowVO;
import com.jc.vo.NodeAttrOutputVo;
import org.flowable.bpmn.model.BpmnModel;


public interface FlowAbleService {

     boolean checkFlowNameExists(String flowName, String processKey);

    ApiResponse createEmptyFlow(String flowName);

    ApiResponse updateFlowName(String processKey, String newName);

    BpmnModel convert(JsonToFlowVO vo);

    ApiResponse<FlowChartVO> getFlowChart(String processInstanceId);

    ApiResponse<NodeAttrOutputVo> getNodeAttr(String pnodeid);
}
