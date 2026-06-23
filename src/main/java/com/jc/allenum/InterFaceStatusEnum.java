package com.jc.allenum;

import lombok.Getter;

@Getter
public enum InterFaceStatusEnum {
    STATUS1("01", "待对接"),
    STATUS2("02", "已对接"),
    STATUS3("03", "测试中"),
    STATUS4("04", "已分离")
    ;

    /** 消息编码（前后端约定标识） */
    private final String code;
    /** 消息描述 */
    private final String desc;

    InterFaceStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static InterFaceStatusEnum[] getAll() {
        return InterFaceStatusEnum.values();
    }
}
