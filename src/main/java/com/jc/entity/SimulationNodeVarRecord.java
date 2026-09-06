package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("CF_SIMULATION_NODE_VAR_RECORD")
@Schema(description = "仿真节点变量记录实体")
@Data
public class SimulationNodeVarRecord implements Serializable {

    @TableId(value = "VAR_RECORD_ID", type = IdType.ASSIGN_UUID)
    @Schema(description = "变量记录ID")
    private String varRecordId;

    @TableField("RUN_ID")
    @Schema(description = "关联的仿真运行ID")
    private String runId;

    @TableField("NODE_ID")
    @Schema(description = "节点ID")
    private String nodeId;

    @TableField("NODE_NAME")
    @Schema(description = "节点名称(冗余)")
    private String nodeName;

    @TableField("VAR_NAME")
    @Schema(description = "变量名称")
    private String varName;

    @TableField("VAR_VALUE")
    @Schema(description = "变量值")
    private String varValue;

    @TableField("VAR_TYPE")
    @Schema(description = "变量类型: string/integer/boolean/number/object/array")
    private String varType;

    @TableField("IS_INPUT")
    @Schema(description = "是否输入变量")
    private Boolean isInput;

    @TableField("IS_OUTPUT")
    @Schema(description = "是否输出变量")
    private Boolean isOutput;

    @TableField("CAPTURE_TIME")
    @Schema(description = "捕获时间(仿真时间)")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime captureTime;

    @TableField("SEQUENCE_NO")
    @Schema(description = "序列号(用于顺序追踪)")
    private Integer sequenceNo;

    @TableField("IS_INITIAL")
    @Schema(description = "是否初始值")
    private Boolean isInitial;

    @TableField("IS_FINAL")
    @Schema(description = "是否最终值")
    private Boolean isFinal;

    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}