package com.jc.allenum;

import lombok.Getter;

@Getter
//设施类型枚举
public enum InterFaceTypeEnum {
    TYPE1("01", "机械分离连接器"),
    TYPE2("02", "星箭供电电缆"),
    TYPE3("03", "地面测试信号线"),
    TYPE4("04", "推进剂加注管路"),
    ;

    /** 消息编码（前后端约定标识） */
    private final String code;
    /** 消息描述 */
    private final String desc;

    InterFaceTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StatusEnum[] getAll() {
        return StatusEnum.values();
    }
}
