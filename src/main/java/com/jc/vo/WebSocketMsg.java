package com.jc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 统一消息结构体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "websocket返回结构")
public class WebSocketMsg<T> {
    @Schema(description = "消息类型编码")
    private String type;

    @Schema(description = "消息业务数据")
    private T data;

    @Schema(description = "消息标题")
    private String title;

    @Schema(description = "状态:0成功，1失败")
    private Integer status;
}
