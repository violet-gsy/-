package com.jc.allenum;

import lombok.Getter;

@Getter
//组件类型枚举
public enum SubSysTypeEnum {
    TYPE1("01", "火箭"),
    TYPE2("02", "船器箭组合体"),
    TYPE3("03", "飞船着陆器"),
    TYPE4("04", "通用"),
    ;

    /** 消息编码（前后端约定标识） */
    private final String code;
    /** 消息描述 */
    private final String desc;

    SubSysTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StatusEnum[] getAll() {
        return StatusEnum.values();
    }
}
