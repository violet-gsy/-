package com.jc.allenum;

import lombok.Getter;

@Getter
public enum MessageTypeEnum {
    STARTENODE("startNode", "启动节点"),
    UPDATENODE("updateNode", "更新节点"),
    ENDNODE("endNode", "结束节点");

    /** 消息编码（前后端约定标识） */
    private final String code;
    /** 消息描述 */
    private final String desc;

    MessageTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
