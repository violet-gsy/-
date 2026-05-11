package com.jc.util;

import lombok.Getter;

@Getter
public enum MessageTypeEnum {

    UPDATENODE("updateNode", "更新节点");

    /** 消息编码（前后端约定标识） */
    private final String code;
    /** 消息描述 */
    private final String desc;

    MessageTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
