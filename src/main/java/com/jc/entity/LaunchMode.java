package com.jc.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@TableName(value = "CF_LAUNCH_MODE")
@Schema(description = "测发模式实体")
@Data
public class LaunchMode implements Serializable {

    @TableId(value = "LAUNCH_MODE_ID", type = IdType.ASSIGN_UUID)
    @Schema(description = "测发模式ID")
    private String launchModeId;

    @TableField(value = "LAUNCH_MODE_NAME")
    @Schema(description = "测发模式名称")
    private String launchModeName;

    @TableField(value = "LAUNCH_MODE_CODE")
    @Schema(description = "测发模式编码")
    private String launchModeCode;

    @TableField(value = "DESCRIPTION")
    @Schema(description = "描述")
    private String description;

    @TableField(value = "SORT_ORDER")
    @Schema(description = "排序")
    private Integer sortOrder;

    @TableField(value = "STATUS")
    @Schema(description = "状态 0-禁用 1-启用")
    private String status;

    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(value = "UPDATE_TIME", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField(value = "CREATE_BY", fill = FieldFill.INSERT)
    @Schema(description = "创建人")
    private String createBy;

    @TableField(value = "UPDATE_BY", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人")
    private String updateBy;

    @TableField(exist = false)
    @Schema(description = "关联的流程列表")
    private List<LaunchModeDiagram> diagrams;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}