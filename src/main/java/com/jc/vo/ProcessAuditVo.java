package com.jc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ProcessAuditVo", description = "流程审批请求参数")
public class ProcessAuditVo {
    @Schema(description = "流程任务ID")
    private String taskId;

    @Schema(description = "审批意见")
    private String comment;

    /**
     * 审批结果
     * 【固定值，不能乱写】
     * agree = 同意
     * reject = 拒绝
     */
    @Schema(
            description = "审批结果（固定值！agree=同意，reject=拒绝）",
            required = true,
            allowableValues = "agree,reject",
            example = "agree"
    )
    private String auditResult;
}
