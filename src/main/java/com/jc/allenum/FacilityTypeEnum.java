package com.jc.allenum;

import lombok.Getter;

@Getter
//设施类型枚举
public enum FacilityTypeEnum {
    TYPE1("01", "厂房"),
    TYPE2("02", "发射工位"),
    TYPE3("03", "供电"),
    TYPE4("04", "空调通风"),
    TYPE5("05", "供气"),
    TYPE6("06", "报警"),
    TYPE7("07", "用水排水"),
    TYPE8("08", "工作间"),
    TYPE9("09", "大门"),
    TYPE10("10", "加注厅"),
    TYPE11("11", "机房"),
    TYPE12("12", "库房"),
    TYPE13("13", "升降平台")
    ;

    /** 消息编码（前后端约定标识） */
    private final String code;
    /** 消息描述 */
    private final String desc;

    FacilityTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StatusEnum[] getAll() {
        return StatusEnum.values();
    }
}
