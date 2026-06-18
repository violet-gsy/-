package com.jc.allenum;

import lombok.Getter;

@Getter
//工器具类型枚举
public enum ToolEquipmentTypeEnum {
    TYPE1("01", "紧固力矩类工具"),
    TYPE2("02", "管路流体专用工器具"),
    TYPE3("03", "电气电缆测试工器具"),
    TYPE4("04", "吊装起重辅助工器具"),
    TYPE5("05", "测量计量类工器具"),
    TYPE6("06", "洁净无尘专用工器具"),
    TYPE7("07", "剪切打磨修配工器具"),
    TYPE8("08", "液压气动类便携工具"),
    TYPE9("09", "通用安全辅助工器具")
    ;

    /** 消息编码（前后端约定标识） */
    private final String code;
    /** 消息描述 */
    private final String desc;

    ToolEquipmentTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StatusEnum[] getAll() {
        return StatusEnum.values();
    }
}
