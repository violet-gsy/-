package com.jc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "空流程创建/修改请求")
public class EmptyFlowVO {
    @Schema(description = "流程唯一标识（修改时必传）")
    private String processKey;

    @Schema(description = "流程名称")
    private String flowName;
}
