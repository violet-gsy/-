package com.jc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "保存关联关系输入VO")
public class RelationInputVO {

    @Schema(description = "我方id")
    private String ourId;

    @Schema(description = "我方类型")
    private String ourType;

    @Schema(description = "目标信息")
    private Map<String, List<String>> targetInfo;




}