package com.jc.allenum;

import lombok.Getter;

@Getter
//组件类型枚举
public enum ProductCptTypeEnum {
    TYPE1("01", "箭体结构"),
    TYPE2("02", "推进系统"),
    TYPE3("03", "控制系统"),
    TYPE4("04", "分离系统"),
    TYPE5("05", "返回舱"),
    TYPE6("06", "轨道舱"),
    TYPE7("07", "推进舱"),
    TYPE8("08", "结构分系统"),
    TYPE9("09", "姿控导航"),
    TYPE10("10", "测控通信"),
    TYPE11("11", "飞船通用机电配套组件"),
    TYPE12("12", "舱外与应急救生组件"),
    ;

    /** 消息编码（前后端约定标识） */
    private final String code;
    /** 消息描述 */
    private final String desc;

    ProductCptTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StatusEnum[] getAll() {
        return StatusEnum.values();
    }
}
