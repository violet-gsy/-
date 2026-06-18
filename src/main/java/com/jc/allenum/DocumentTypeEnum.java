package com.jc.allenum;

import lombok.Getter;

@Getter
//文档类型枚举
public enum DocumentTypeEnum {
    TYPE1("01", "任务文书"),
    TYPE2("02", "技术手册"),
    ;

    /** 消息编码（前后端约定标识） */
    private final String code;
    /** 消息描述 */
    private final String desc;

    DocumentTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StatusEnum[] getAll() {
        return StatusEnum.values();
    }
}
