package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@TableName("CF_SIMULATION_NODE_RECORD")
@Schema(description = "仿真节点运行记录实体")
@Data
public class SimulationNodeRecord implements Serializable {

    @TableId(value = "RECORD_ID", type = IdType.ASSIGN_UUID)
    @Schema(description = "记录ID")
    private String recordId;

    @TableField("RUN_ID")
    @Schema(description = "关联的仿真运行ID")
    private String runId;

    @TableField("NODE_ID")
    @Schema(description = "节点ID(来自MagicDraw)")
    private String nodeId;

    @TableField("NODE_NAME")
    @Schema(description = "节点名称")
    private String nodeName;

    @TableField("NODE_TYPE")
    @Schema(description = "节点类型: Action/Initial/Final/Decision/Fork/Join等")
    private String nodeType;

    @TableField("EXECUTION_STATUS")
    @Schema(description = "执行状态: idle/ready/waiting/running/done/error")
    private String executionStatus;

    @TableField("READY_TIME")
    @Schema(description = "就绪时间(仿真时间)")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readyTime;

    @TableField("START_TIME")
    @Schema(description = "开始执行时间(仿真时间)")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @TableField("FINISH_TIME")
    @Schema(description = "完成时间(仿真时间)")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    @TableField("WAIT_DURATION_MS")
    @Schema(description = "等待时长(毫秒)")
    private Long waitDurationMs;

    @TableField("EXEC_DURATION_MS")
    @Schema(description = "执行时长(毫秒)")
    private Long execDurationMs;

    @TableField("TOTAL_DURATION_MS")
    @Schema(description = "总耗时(毫秒)")
    private Long totalDurationMs;

    @TableField("REQUIRED_PEOPLE")
    @Schema(description = "需要人员数量")
    private Integer requiredPeople;

    @TableField("ASSIGNED_PEOPLE")
    @Schema(description = "实际分配人员")
    private Integer assignedPeople;

    @TableField("QUEUE_POSITION")
    @Schema(description = "队列位置(0表示不在队列中)")
    private Integer queuePosition;

    @TableField("QUEUE_SIZE")
    @Schema(description = "队列总大小")
    private Integer queueSize;

    @TableField("EXECUTION_ORDER")
    @Schema(description = "执行顺序号")
    private Integer executionOrder;

    @TableField("IS_START_NODE")
    @Schema(description = "是否起始节点")
    private Boolean isStartNode;

    @TableField("IS_END_NODE")
    @Schema(description = "是否结束节点")
    private Boolean isEndNode;

    @TableField("NODE_DESCRIPTION")
    @Schema(description = "节点描述/Body内容")
    private String nodeDescription;

    @TableField("ERROR_MESSAGE")
    @Schema(description = "错误信息")
    private String errorMessage;

    // ========== 新增字段 ==========
    @TableField("NODE_VARS")
    @Schema(description = "节点变量值(JSON格式，包含所有ResourceTask属性)")
    private String nodeVars;

    @TableField(exist = false)
    @Schema(description = "节点变量值(对象形式)")
    private Map<String, Object> nodeVarsMap;

    @TableField("STEREOTYPE_NAME")
    @Schema(description = "应用的构造型名称(如ResourceTask)")
    private String stereotypeName;

    @TableField("ALL_PROPERTIES")
    @Schema(description = "所有构造型属性(JSON格式)")
    private String allProperties;

    @TableField(exist = false)
    @Schema(description = "所有构造型属性(对象形式)")
    private Map<String, Object> allPropertiesMap;

    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(value = "UPDATE_TIME", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}