package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("CF_SIMULATION_VARIABLE")
@Schema(description = "仿真变量实体")
@Data
public class SimulationVariable implements Serializable {

    @TableId(value = "ID", type = IdType.ASSIGN_UUID)
    @Schema(description = "主键ID")
    private String id;

    @TableField("RUN_ID")
    @Schema(description = "运行ID")
    private String runId;

    @TableField("VARIABLE_NAME")
    @Schema(description = "变量名")
    private String variableName;

    @TableField("VARIABLE_VALUE")
    @Schema(description = "变量值")
    private String variableValue;

    @TableField("VALUE_TYPE")
    @Schema(description = "值类型")
    private String valueType;

    @TableField("TIMESTAMP")
    @Schema(description = "记录时间戳")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @TableField("SEQUENCE_NO")
    @Schema(description = "序列号")
    private Integer sequenceNo;

    @TableField(value = "CREATETIME", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createtime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}