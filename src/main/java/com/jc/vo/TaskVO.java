package com.jc.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;

@Data
@Schema(description = "待办任务VO")
public class TaskVO {

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "流程实例ID")
    private String processInstanceId;

    @Schema(description = "流程定义ID")
    private String processDefinitionId;

    @Schema(description = "审批人")
    private String assignee;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "业务ID")
    private String businessKey;
}