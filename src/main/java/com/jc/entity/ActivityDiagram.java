package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName(value = "CF_ACTIVITY_DIAGRAM")
@Schema(description = "活动图表实体")
@Data
public class ActivityDiagram implements Serializable {

    @TableId(value = "ACTIVITY_DIAGRAM_ID", type = IdType.ASSIGN_UUID)
    @Schema(description = "活动图ID")
    private String activityDiagramId;

    @TableField(value = "ACTIVITY_DIAGRAM_NAME")
    @Schema(description = "活动图名称")
    private String activityDiagramName;

    @TableField(value = "LAUNCH_MODE_ID")
    @Schema(description = "测发模式ID")
    private String launchModeId;

    @TableField(exist = false)
    @Schema(description = "测发模式名称（非数据库字段，用于前端展示）")
    private String launchModeName;

    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(value = "UPDATE_TIME", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}