package com.jc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(name = "StartFlowInputVO", description = "启动流程输入参数")
public class StartFlowInputVO {

    @Schema(description = "流程定义key，")
    private String processKey;

    @Schema(description = "业务主键ID（自定义）")
    private String businessKey;

}
