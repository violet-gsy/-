package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName(value = "CF_LAUNCH_MODE_DIAGRAM")
@Schema(description = "测发模式-流程关联实体")
@Data
public class LaunchModeDiagram implements Serializable {

    @TableId(value = "ID", type = IdType.ASSIGN_UUID)
    @Schema(description = "主键ID")
    private String id;

    @TableField(value = "LAUNCH_MODE_ID")
    @Schema(description = "测发模式ID")
    private String launchModeId;

    @TableField(value = "ACTIVITY_DIAGRAM_ID")
    @Schema(description = "活动图ID")
    private String activityDiagramId;

    @TableField(value = "DIAGRAM_NAME")
    @Schema(description = "图名称")
    private String diagramName;

    @TableField(value = "SORT_ORDER")
    @Schema(description = "排序")
    private Integer sortOrder;

    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(value = "UPDATE_TIME", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    @Schema(description = "关联的测发模式名称")
    private String launchModeName;

    @TableField(exist = false)
    @Schema(description = "关联的流程图对象")
    private ActivityDiagram activityDiagram;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}