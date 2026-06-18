package com.jc.allenum;

import lombok.Getter;

@Getter
public enum StatusEnum {
    USE("use", "使用中"),
    NOUSE("nouse", "未使用")
    ;

    /** 消息编码（前后端约定标识） */
    private final String code;
    /** 消息描述 */
    private final String desc;

    StatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StatusEnum[] getAll() {
        return StatusEnum.values();
    }
}
