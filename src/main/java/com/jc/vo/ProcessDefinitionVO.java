package com.jc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * Flowable 流程定义 VO (Swagger3 版)
 * 前端 + 接口文档 标准返回体
 */
@Data
@Schema(name = "ProcessDefinitionVO", description = "流程定义信息")
public class ProcessDefinitionVO {

    @Schema(description = "部署ID")
    private String deploymentId;

    @Schema(description = "部署名称")
    private String deploymentName;

    @Schema(description = "部署时间（创建时间）")
    private Date deploymentTime;

    @Schema(description = "流程定义ID")
    private String processDefinitionId;

    @Schema(description = "流程KEY")
    private String processKey;

    @Schema(description = "流程名称")
    private String processName;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "流程资源文件名称")
    private String resourceName;

    @Schema(description = "流程图资源名称")
    private String diagramResourceName;

    @Schema(description = "流程描述")
    private String description;

    @Schema(description = "是否挂起：true-已挂起 false-正常")
    private Boolean suspended;

    @Schema(description = "流程状态：正常/已挂起")
    private String status;

    @Schema(description = "租户ID")
    private String tenantId;
}

