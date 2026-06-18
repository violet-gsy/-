package com.jc.allenum;

import lombok.Getter;

@Getter
//工装类型枚举
public enum ToolFixtrueTypeEnum {
    TYPE1("01", "吊装吊具类工装"),
    TYPE2("02", "支撑停放型架工装"),
    TYPE3("03", "转运转载移动工装"),
    TYPE4("04", "对接装配定位工装"),
    TYPE5("05", "发射工位专用固定工装"),
    TYPE6("06", "加注供气专用工装"),
    TYPE7("07", "测试与电气专用工装"),
    TYPE8("08", "翻转调姿专用工装"),
    ;

    /** 消息编码（前后端约定标识） */
    private final String code;
    /** 消息描述 */
    private final String desc;

    ToolFixtrueTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StatusEnum[] getAll() {
        return StatusEnum.values();
    }
}
