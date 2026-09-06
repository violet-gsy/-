package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@TableName("CF_SIMULATION_RUN")
@Schema(description = "仿真运行记录实体")
@Data
public class SimulationRun implements Serializable {

    @TableId(value = "ID", type = IdType.ASSIGN_UUID)
    @Schema(description = "运行ID")
    private String id;

    @TableField("DIAGRAM_ID")
    @Schema(description = "流程图ID")
    private String diagramId;

    @TableField("DIAGRAM_NAME")
    @Schema(description = "流程图名称")
    private String diagramName;

    @TableField("START_MODE")
    @Schema(description = "启动模式")
    private String startMode;

    @TableField("CONTEXT_NAME")
    @Schema(description = "上下文名称")
    private String contextName;

    @TableField("STATUS")
    @Schema(description = "状态")
    private String status;

    @TableField("START_TIME")
    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @TableField("END_TIME")
    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @TableField("DURATION")
    @Schema(description = "持续时间(毫秒)")
    private Long duration;

    @TableField("USER_ID")
    @Schema(description = "操作用户ID")
    private String userId;

    @TableField("REMARK")
    @Schema(description = "备注")
    private String remark;

    // ========== 新增字段 ==========
    @TableField("INIT_VARS")
    @Schema(description = "初始化变量值(JSON格式)")
    private String initVars;

    @TableField(exist = false)
    @Schema(description = "初始化变量值(对象形式，用于API交互)")
    private Map<String, Object> initVarsMap;

    @TableField(value = "CREATETIME", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createtime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}