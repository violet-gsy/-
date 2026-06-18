package com.jc.allenum;

import lombok.Getter;

@Getter
//相态类型枚举
public enum PhaseTypeEnum {
    YT("YT", "液态"),
    QT("QT", "气态"),
    GT("GT", "固态")
    ;

    /** 消息编码（前后端约定标识） */
    private final String code;
    /** 消息描述 */
    private final String desc;

    PhaseTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StatusEnum[] getAll() {
        return StatusEnum.values();
    }
}
