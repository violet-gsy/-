package com.jc.allenum;

import lombok.Getter;

@Getter
//设备类型枚举
public enum EquipmentTypeEnum {
    TYPE1("01", "火箭卫星总装测试设备"),
    TYPE2("02", "垂直总装与发射平台设备"),
    TYPE3("03", "发射工位固定设备"),
    TYPE4("04", "推进剂加注与供气低温设备"),
    TYPE5("05", "转运运载大型设备"),
    TYPE6("06", "测控、通信、时统供电设备"),
    TYPE7("07", "保障设备"),
    ;

    /** 消息编码（前后端约定标识） */
    private final String code;
    /** 消息描述 */
    private final String desc;

    EquipmentTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StatusEnum[] getAll() {
        return StatusEnum.values();
    }
}
