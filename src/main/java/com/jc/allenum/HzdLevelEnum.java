package com.jc.allenum;

import lombok.Getter;

@Getter
//危险等级枚举
public enum HzdLevelEnum {
    NON("NON", "无危险"),
    LWO("LWO", "低危险"),
    MEDIUM("MEDIUM", "中危险"),
    HIGH("HEIH", "高危险")
    ;

    /** 消息编码（前后端约定标识） */
    private final String code;
    /** 消息描述 */
    private final String desc;

    HzdLevelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StatusEnum[] getAll() {
        return StatusEnum.values();
    }
}
