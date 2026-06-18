package com.jc.allenum;

import lombok.Getter;

@Getter
//介质类型枚举
public enum FluidTypeEnum {
    TYPE1("01", "气体"),
    TYPE2("02", "液体"),
    ;

    /** 消息编码（前后端约定标识） */
    private final String code;
    /** 消息描述 */
    private final String desc;

    FluidTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StatusEnum[] getAll() {
        return StatusEnum.values();
    }
}
