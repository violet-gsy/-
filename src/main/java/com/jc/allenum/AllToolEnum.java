package com.jc.allenum;

import lombok.Getter;

@Getter
//全量，关联用
public enum AllToolEnum {
    LAUNCHSITE("/launchSite", "发射场"),
    LSAREA("/lsarea", "发射场区域"),
    LSKEYFACILITY("/lsKeyfacility", "发射场设施"),
    PRODUCT("/product", "产品"),
    PRODUCTCOMPONENT("/productComponent", "产品组件"),
    LSEQUIPMENT("/lsEquipment", "设备"),
    TOOLFIXTRUE("/toolFixture", "工装"),
    TOOLEQUIPMENT("/toolEquipment", "工器具"),
    MAJOR("/major", "专业"),
    PERSONNEL("/personnel", "人员"),
    LAUNCHSITEFLUID("/launchSiteFluid", "发射场流体介质"),
    DOCUMENT("/document", "文档"),
    INTERFACE("/interface", "接口关系"),
    ;

    /** 消息编码（前后端约定标识） */
    private final String code;
    /** 消息描述 */
    private final String desc;

    AllToolEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AllToolEnum[] getAll() {
        return AllToolEnum.values();
    }
}
