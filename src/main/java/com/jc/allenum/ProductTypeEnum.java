package com.jc.allenum;

import lombok.Getter;

@Getter
//产品类型枚举
public enum ProductTypeEnum {
    TYPE1("01", "火箭"),
    TYPE2("02", "飞船"),
    TYPE3("03", "航天器")
    ;

    /** 消息编码（前后端约定标识） */
    private final String code;
    /** 消息描述 */
    private final String desc;

    ProductTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StatusEnum[] getAll() {
        return StatusEnum.values();
    }
}
